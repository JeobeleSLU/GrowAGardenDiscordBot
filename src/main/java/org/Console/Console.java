package org.Console;

import java.util.Scanner;
import org.Bot.StockBot;

public class Console implements Runnable {
    private final Scanner scanner = new Scanner(System.in);
    private final ConsoleMessage alert;

    public Console(ConsoleMessage alert) {
       this.alert = alert;
    }

    @Override
    public void run() {
        System.out.println("Console started. Type 'help' for commands.");
        while (true) {
            System.out.print(">> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Exiting bot...");
                System.exit(0);
            } else if (input.toLowerCase().startsWith("broadcast ")) {
                String message = input.substring("broadcast ".length()).trim();
                if (!message.isEmpty()) {
                    alert.sendMessage(message);
                } else {
                    System.out.println("Cannot broadcast empty message.");
                }
            } else if (input.toLowerCase().startsWith("mention ")) {
                String message = input.substring("mention ".length()).trim();
                if (!message.isEmpty()) {
                    alert.mentionRoleAndSend(message);
                } else {
                    System.out.println("Cannot mention with empty message.");
                }
            } else if (input.equalsIgnoreCase("help")) {
                System.out.println("Available commands:");
                System.out.println(" - broadcast <message>");
                System.out.println(" - mention <message>");
                System.out.println(" - exit");
            } else {
                System.out.println("Unknown command. Type 'help' for options.");
            }
        }
    }
}
