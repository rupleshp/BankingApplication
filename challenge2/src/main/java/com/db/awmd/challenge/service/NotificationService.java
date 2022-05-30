package com.db.awmd.challenge.service;

import org.springframework.stereotype.Component;

import com.db.awmd.challenge.domain.Account;

@Component
public interface NotificationService {

  void notifyAboutTransfer(Account account, String transferDescription);
}
