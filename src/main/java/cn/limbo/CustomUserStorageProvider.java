package cn.limbo;

import cn.limbo.dto.CustomUser;
import cn.limbo.dto.RespGetUser;
import cn.limbo.dto.RespValidate;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.UserCredentialManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SubjectCredentialManager;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.ReadOnlyException;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.adapter.AbstractUserAdapter;
import org.keycloak.storage.user.UserLookupProvider;

public class CustomUserStorageProvider implements UserStorageProvider, UserLookupProvider,
    CredentialInputValidator, CredentialInputUpdater {

  private static final Logger logger = Logger.getLogger(CustomUserStorageProvider.class);

  protected KeycloakSession session;
  protected ComponentModel model;
  protected HttpClient httpClient = HttpClient.newHttpClient();
  protected Map<String, UserModel> loadedUsers = new HashMap<>();

  public CustomUserStorageProvider(KeycloakSession session, ComponentModel model) {
    this.session = session;
    this.model = model;
  }

  @Override
  public void close() {

  }

  @Override
  public UserModel getUserById(RealmModel realm, String id) {
    var storageId = new StorageId(id);
    var username = storageId.getExternalId();
    logger.info("External user id: " + username);
    return this.getUserByUsername(realm, username);
  }

  protected UserModel createAdapter(RealmModel realm, String username) {
    return new AbstractUserAdapter(session, realm, model) {
      @Override
      public String getUsername() {
        return username;
      }

      @Override
      public SubjectCredentialManager credentialManager() {
        return new UserCredentialManager(session, realm, this);
      }
    };
  }

  @Override
  public UserModel getUserByUsername(RealmModel realm, String username) {
    var adapter = loadedUsers.get(username);
    if (adapter == null) {
      var user = getUserFromExternalService(realm.getId(), username);
      adapter = createAdapter(realm, username);
      loadedUsers.put(username, adapter);
    }
    return adapter;
  }

  @Override
  public UserModel getUserByEmail(RealmModel realm, String email) {
    return null;
  }

  /**
   * Retrieves a user from an external service based on the tenant ID and login name.
   *
   * @param tenantId  the ID of the tenant
   * @param loginName the login name of the user
   * @return the user model retrieved from the external service
   * @throws RuntimeException if there is an error retrieving the user from the external service
   */
  private CustomUser getUserFromExternalService(String tenantId, String loginName) {
    logger.info("Get user from external service");

    var request = HttpRequest.newBuilder()
        .POST(HttpRequest.BodyPublishers.ofString("{}"))
        .uri(java.net.URI.create(
            model.getConfig().getFirst("userapi") + "/" + tenantId + "/" + loginName))
        .timeout(Duration.ofSeconds(3))
        .build();

    //send post request
    try {
      var httpResponse = httpClient.send(request,
          java.net.http.HttpResponse.BodyHandlers.ofString());
      logger.info("External service response: " + httpResponse.body());
      var response = new RespGetUser().fromJsonString(httpResponse.body());

      // If the response is null or the user is null, return null
      if (response == null || response.getCustomUser() == null) {
        logger.info("Failed to get user from external service");
        return null;
      }

      logger.info("Get user from external service: " + response.getCustomUser().toString());

      return response.getCustomUser();
    } catch (Exception e) {
      logger.error("Failed to get user from external service");
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean supportsCredentialType(String credentialType) {
    return credentialType.equals(PasswordCredentialModel.TYPE);
  }

  @Override
  public boolean updateCredential(RealmModel realm, UserModel user, CredentialInput input) {
    if (input.getType().equals(PasswordCredentialModel.TYPE)) {
      throw new ReadOnlyException("user is read only for this update");
    }
    return false;
  }

  @Override
  public void disableCredentialType(RealmModel realm, UserModel user, String credentialType) {

  }

  @Override
  public Stream<String> getDisableableCredentialTypesStream(RealmModel realm, UserModel user) {
    return Stream.empty();
  }

  @Override
  public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
    return credentialType.equals(PasswordCredentialModel.TYPE);
  }

  /**
   * Validates the given credentials for a user in the specified realm.
   *
   * @param realm           the realm to which the user belongs
   * @param user            the user for whom the credentials are being validated
   * @param credentialInput the credential input to be validated
   * @return {@code true} if the credentials are valid, {@code false} otherwise
   */
  @Override
  public boolean isValid(RealmModel realm, UserModel user, CredentialInput credentialInput) {
    logger.info("Validate credentials");
    if (!supportsCredentialType(credentialInput.getType())) {
      return false;
    }
    String tenantId = realm.getId();
    String username = user.getUsername();
    String password = credentialInput.getChallengeResponse();
    logger.info(String.format("Tenant ID = %s Username = %s Password = %s", tenantId, username,
        password));
    return externalValidate(tenantId, username, password);
  }

  /**
   * Validates the given credentials with an*/
  private boolean externalValidate(String tenantId, String username,
      String inputPwd) {
    logger.info(
        String.format("Validate credentials with external service. Username = %s Password = %s",
            username, inputPwd));
    var body = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, inputPwd);
    var request = HttpRequest.newBuilder()
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .header("Content-Type", "application/json")
        .uri(java.net.URI.create(
            model.getConfig().getFirst("validateapi") + "/" + tenantId))
        .timeout(Duration.ofSeconds(3))
        .build();
    try {
      var httpResponse = httpClient.send(request,
          java.net.http.HttpResponse.BodyHandlers.ofString());
      logger.info("External service response: " + httpResponse.body());
      var response = new RespValidate().fromJsonString(httpResponse.body());
      return response.isValid();
    } catch (Exception e) {
      return false;
    }
  }


}
