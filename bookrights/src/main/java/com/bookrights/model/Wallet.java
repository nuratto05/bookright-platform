package com.bookrights.model;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity 
@Table(name = "Wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_Id", nullable = false)
    private User user;

    @Column (name = "pending_balance", nullable = false)
    private double pending_balance;

    @Column (name = "balance", nullable = false)
    private double balance;

    public Wallet() {

    }

    public Wallet(User user, double balance) {
        this.user = user;
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double getPending_balance() {
        return pending_balance;
    }

    public void setPending_balance(double pending_balance) {
        if(pending_balance < 0) {
            throw new IllegalArgumentException("Pending balance cannot be negative");
        }else if(pending_balance < getBalance()) {
            throw new IllegalArgumentException("Balance too low");
        }

        this.pending_balance = pending_balance;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        if(pending_balance < 0) {
            throw new IllegalArgumentException("Pending balance cannot be negative");
        }
        this.balance = balance;
    }
    
}
