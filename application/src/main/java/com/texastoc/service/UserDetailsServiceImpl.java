package com.texastoc.service;

import com.texastoc.model.user.Player;
import com.texastoc.repository.PlayerRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  private final PlayerRepository playerRepository;

  public UserDetailsServiceImpl(PlayerRepository playerRepository) {
    this.playerRepository = playerRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    Player player = playerRepository.getByEmail(email);
    if (player == null) {
      throw new UsernameNotFoundException(email);
    }
    return new org.springframework.security.core.userdetails.User(player.getEmail(), player.getPassword(), getAuthority(player));
  }

  private Set<SimpleGrantedAuthority> getAuthority(Player player) {
    Set<SimpleGrantedAuthority> authorities = new HashSet<>();
    player.getRoles().forEach(role -> {
      authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
    });
    return authorities;
  }

}
