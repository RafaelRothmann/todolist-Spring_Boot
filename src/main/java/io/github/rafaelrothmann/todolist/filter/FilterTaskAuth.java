package io.github.rafaelrothmann.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import io.github.rafaelrothmann.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.var;

@Component
public class FilterTaskAuth extends OncePerRequestFilter{

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
       
        var servletPath = request.getServletPath();

        if (servletPath.startsWith("/tasks/")){
            var userPassword = request.getHeader("Authorization").substring("Basic".length()).trim();
    
            byte[] authDecode = Base64.getDecoder().decode(userPassword);
    
            String authString = new String(authDecode);
            String[] credentials =  authString.split(":");
            String userName = credentials[0];
            String userPass = credentials[1];
    
            var user = this.userRepository.findByUsername(userName);
            if (user == null){
                response.sendError(401);
            } else {
                var passwordVerify = BCrypt.verifyer().verify(userPass.toCharArray(), user.getPassword());
                if (passwordVerify.verified){
                    request.setAttribute("idUser", user.getId());
                    filterChain.doFilter(request, response);
                } else {
                    response.sendError(401);
                }
            }   
        } else {
            filterChain.doFilter(request, response);
        }

    }
    
}
