package com.nowcoder.community.entity;

import lombok.Data;

import java.util.Date;

@Data
public class LoginTicket {

    private int id;
    private int userId;
    private String Ticket;
    private int status;
    private Date expired;

    @Override
    public String toString() {
        return "LoginTicket{" +
                "id=" + id +
                ", userId=" + userId +
                ", Ticket='" + Ticket + '\'' +
                ", status=" + status +
                ", expired=" + expired +
                '}';
    }
}
