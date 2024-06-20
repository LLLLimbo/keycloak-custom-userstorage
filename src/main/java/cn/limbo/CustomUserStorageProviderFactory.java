package cn.limbo;

import java.util.List;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;

public class CustomUserStorageProviderFactory implements
    UserStorageProviderFactory<CustomUserStorageProvider> {

  protected static final List<ProviderConfigProperty> configMetadata;

  static {
    configMetadata = ProviderConfigurationBuilder.create()
        .property().name("userapi")
        .type(ProviderConfigProperty.STRING_TYPE)
        .label("User information api")
        .defaultValue("http://172.21.208.1:17010/user/info")
        .helpText("用户信息接口地址")
        .add()
        .property()
        .name("validateapi")
        .type(ProviderConfigProperty.STRING_TYPE)
        .label("Validate api")
        .defaultValue("http://172.21.208.1:17010/user/validate")
        .helpText("用户名密码验证接口地址")
        .add()
        .build();
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return configMetadata;
  }


  @Override
  public CustomUserStorageProvider create(KeycloakSession keycloakSession,
      ComponentModel componentModel) {
    return new CustomUserStorageProvider(keycloakSession, componentModel);
  }

  @Override
  public String getId() {
    return "custom-user-storage-provider";
  }
}
