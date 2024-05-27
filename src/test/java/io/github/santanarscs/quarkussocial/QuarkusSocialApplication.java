package io.github.santanarscs.quarkussocial;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;

import jakarta.ws.rs.core.Application;

@OpenAPIDefinition(info = @Info(title = "Api Quarkus SOcial", version = "1.0"

))
public class QuarkusSocialApplication extends Application {

}
