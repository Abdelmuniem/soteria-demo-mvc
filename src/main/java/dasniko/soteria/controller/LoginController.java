package dasniko.soteria.controller;

import dasniko.soteria.model.Login;

import javax.inject.Inject;
import javax.mvc.Models;
import javax.mvc.annotation.Controller;
import javax.mvc.binding.BindingResult;
import javax.mvc.binding.ValidationError;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.SecurityContext;
import javax.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import javax.security.enterprise.credential.Credential;
import javax.security.enterprise.credential.Password;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.executable.ExecutableType;
import javax.validation.executable.ValidateOnExecution;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Niko Köbler, http://www.n-k.de, @dasniko
 */
@Path("login")
@Controller
public class LoginController {

    @Inject
    private Models models;
    @Inject
    private SecurityContext securityContext;
    @Inject
    private BindingResult bindingResult;
    @Context
    private HttpServletRequest request;
    @Context
    private HttpServletResponse response;

    @GET
    public String index() {
        return "login.jsp";
    }

    @POST
    @ValidateOnExecution(type = ExecutableType.NONE)
    public String login(@Valid @BeanParam Login login) {
        if (bindingResult.isFailed()) {
            List<String> errors = bindingResult.getAllValidationErrors().stream()
                .map(ValidationError::getMessage)
                .collect(Collectors.toList());
            models.put("errors", errors);
            return index();
        }

        Credential credential = new UsernamePasswordCredential(login.getUsername(), new Password(login.getPassword()));

        AuthenticationStatus authStatus = securityContext.authenticate(request, response,
            AuthenticationParameters.withParams()
                .credential(credential)
                .newAuthentication(true)
                .rememberMe(login.isRememberMe())
        );

        if (authStatus.equals(AuthenticationStatus.SUCCESS)) {
            return "redirect:user";
        } else if (authStatus.equals(AuthenticationStatus.SEND_FAILURE)) {
            models.put("errors", "Error during authentication: username and/or password not correct.");
            return index();
        } else {
            models.put("errors", "Unexpected error during authentication: " + authStatus.name());
            return index();
        }
    }
}
