package com.texastoc.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class AuthorizationHelper {

  public boolean isLoggedInUserHaveRole(SecurityRole securityRole) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Collection<? extends GrantedAuthority> grantedAuthorities = authentication.getAuthorities();
    for (GrantedAuthority grantedAuthority : grantedAuthorities) {
      if (("ROLE_" + securityRole.name()).equals(grantedAuthority.toString())) {
        return true;
      }
    }

    return false;
  }

  public String getLoggedInUserEmail() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (principal instanceof UserDetails) {
      return ((UserDetails)principal).getUsername();
    }
    return null;
  }

}
