package com.atlaspay.simulator.domain.repository;

public interface SimulatorAccountRepository {
    long getNextAccountSerial();
    void saveAccount(String id, String reference, String bankName, String bankCode, long accountSerial, String nuban, String accountName, String callbackUrl, String status);
}
