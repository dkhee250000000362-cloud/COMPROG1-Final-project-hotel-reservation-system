package pkgfinal.test;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Dumangas_LianKhee_Davey_HotelReservationSystem {
    static Scanner ken = new Scanner(System.in);
    static String[] roomTypes = {"Standard", "Suite", "Deluxe"};
    static double[] roomPrices = {350, 550, 750};
    static int[] roomAvailability = {5, 5, 5};

    public static void main(String[] args) {
        loadRoomAvailability();

        while (true) {
            System.out.println("\n===============================");
            System.out.println("  KL HOTEL RESERVATION SYSTEM  ");
            System.out.println("===============================");
            System.out.println("1. Create Account");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.println("===============================");
            System.out.print("Enter choice: ");
            int Account = getValidIntInput();

            switch (Account) {
                case 1 -> registerUser();
                case 2 -> {
                    File accountFile = new File("accounts.txt");
                    if (!accountFile.exists() || accountFile.length() == 0) {
                        System.out.println("No existing account found. Please create an account first!");
                    } else {
                        loginUser();
                    }
                }
                case 3 -> {
                    System.out.println("Thank you for using KL Hotel Management System!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid choice, please try again.");
            }
        }
    }

    static int getValidIntInput() {
        while (true) {
            String input = ken.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print("Invalid input! Numbers only. Please try again: ");
            }
        }
    }

    static void registerUser() {
        try (FileWriter fw = new FileWriter("accounts.txt", true)) {
            System.out.print("Enter Username: ");
            String username = ken.nextLine().trim();
            if (username.isEmpty()) {
                System.out.println("Username cannot be empty!");
                return;
            }

            System.out.print("Enter Password (min 8 chars): ");
            String password = ken.nextLine().trim();
            if (password.isEmpty()) {
                System.out.println("Password cannot be empty!");
                return;
            }
            if (password.length() < 8) {
                System.out.println("Password must be at least 8 characters long!");
                return;
            }

            String maskPassword = "*".repeat(password.length());
            System.out.println("Masked password: " + maskPassword);

            fw.write(username + "," + password + "\n");
            fw.flush();
            System.out.println("Account created and saved successfully!");
        } catch (IOException e) {
            System.out.println("Error saving account: " + e.getMessage());
        }
    }

    static void loginUser() {
        File accountFile = new File("accounts.txt");
        if (!accountFile.exists() || accountFile.length() == 0) {
            System.out.println("No account found. Please create one first.");
            return;
        }

        int attempts = 3;
        while (attempts > 0) {
            System.out.print("Enter username: ");
            String username = ken.nextLine().trim();
            System.out.print("Enter password (min 8 chars): ");
            String password = ken.nextLine().trim();

            boolean found = false;
            try (Scanner fileScanner = new Scanner(accountFile)) {
                while (fileScanner.hasNextLine()) {
                    String line = fileScanner.nextLine().trim();
                    if (line.isEmpty()) continue;
                    String[] parts = line.split(",");
                    if (parts.length == 2 && username.equals(parts[0]) && password.equals(parts[1])) {
                        found = true;
                        break;
                    }
                }
            } catch (FileNotFoundException e) {
                System.out.println("Error reading accounts file.");
                return;
            }

            if (found) {
                System.out.println("\nLogin successful! Welcome, " + username + "!");
                roomMenu(username);
                return;
            } else {
                attempts--;
                System.out.println("Invalid username or password. Attempts left: " + attempts);
            }
        }

        System.out.println("Too many failed attempts. Returning to main menu...");
    }

    static void roomMenu(String username) {
        while (true) {
            System.out.println("\n==================================");
            System.out.println("            HOTEL MENU");
            System.out.println("==================================");
            System.out.println("1. View Rooms");
            System.out.println("2. Book a Room");
            System.out.println("3. Logout");
            System.out.println("==================================");
            System.out.print("Enter your choice: ");
            int choice = getValidIntInput();

            switch (choice) {
                case 1 -> showRoomList();
                case 2 -> bookingProcess(username);
                case 3 -> {
                    System.out.println("Logged out successfully!");
                    System.out.println("Thank you for using KL Hotel Management System!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid choice, please try again.");
            }
        }
    }

    static void showRoomList() {
        System.out.println("\nAvailable Rooms:");
        for (int i = 0; i < roomTypes.length; i++) {
            System.out.printf("%d. %s - P%.2f - Available: %d rooms%n", i + 1, roomTypes[i], roomPrices[i], roomAvailability[i]);
        }
    }

    static void bookingProcess(String username) {
        showRoomList();
        System.out.print("\nEnter guest name: ");
        String guestName = ken.nextLine().trim();

        System.out.print("Choose room type (1-3): ");
        int roomChoice = getValidIntInput();
        if (roomChoice < 1 || roomChoice > 3) {
            System.out.println("Invalid room choice.");
            return;
        }

        if (roomAvailability[roomChoice - 1] <= 0) {
            System.out.println("Sorry, this room type is fully booked. Resetting availability to 5...");
            roomAvailability[roomChoice - 1] = 5;
            saveRoomAvailability();
            return;
        }

        System.out.print("Enter number of days to stay: ");
        int days = getValidIntInput();

        System.out.print("Enter your planned check-in date (yyyy-MM-dd): ");
        String checkInInput = ken.nextLine().trim();
        LocalDate checkInDate;
        try {
            checkInDate = LocalDate.parse(checkInInput);
        } catch (Exception e) {
            System.out.println("Invalid date format. Booking canceled.");
            return;
        }

        LocalDate today = LocalDate.now();
        double discount = 0;
        long daysUntilCheckIn = ChronoUnit.DAYS.between(today, checkInDate);

        // ---- Okada-style Discounts ----
        if (daysUntilCheckIn > 30) {
            discount = 0.15; // Early bird 15%
            System.out.println("Early bird discount applied: 15%");
        } else if (daysUntilCheckIn <= 2) {
            discount = 0.05; // Last-minute 5%
            System.out.println("Last-minute discount applied: 5%");
        }

        // Optional: Stay & Dine promo
        System.out.print("Apply Stay & Dine package? yes/no: ");
        String packageChoice = ken.nextLine().trim().toLowerCase();
        if (packageChoice.equals("yes")) {
            discount += 0.10; // 10% promo
            if (discount > 0.25) discount = 0.25; // max 25% discount
            System.out.println("Stay & Dine promo applied: +10%");
        }

        double subtotal = roomPrices[roomChoice - 1] * days;
        double discountedAmount = subtotal * (1 - discount);
        double tax = discountedAmount * 0.12;
        double finalAmount = discountedAmount + tax;

        System.out.println("\n======== BOOKING SUMMARY ========");
        System.out.println("Guest Name: " + guestName);
        System.out.println("Room Type: " + roomTypes[roomChoice - 1]);
        System.out.println("Days: " + days);
        System.out.printf("Subtotal: P%.2f%n", subtotal);
        System.out.printf("Discount: %.0f%%%n", discount * 100);
        System.out.printf("After Discount: P%.2f%n", discountedAmount);
        System.out.printf("Tax (12%%): P%.2f%n", tax);
        System.out.printf("Total Amount: P%.2f%n", finalAmount);
        System.out.println("=================================");

        System.out.print("Proceed to payment? yes/no: ");
        String confirm = ken.nextLine().trim().toLowerCase();
        if (confirm.equals("yes")) {
            roomAvailability[roomChoice - 1]--;
            if (roomAvailability[roomChoice - 1] == 0) {
                System.out.println("Room type fully booked! Resetting availability to 5 rooms.");
                roomAvailability[roomChoice - 1] = 5;
            }
            saveRoomAvailability();

            // Generate receipt
            String checkIn = checkInDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
            String checkOut = checkInDate.plusDays(days).format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
            createReceipt(username, guestName, roomTypes[roomChoice - 1], days,
                    subtotal, discount, tax, finalAmount, checkIn, checkOut);

            System.out.println("Booking successful! Receipt saved.");
        } else {
            System.out.println("Booking canceled.");
        }
    }

    static void createReceipt(String username, String guestName, String roomType, int days,
                              double subtotal, double discount, double tax, double total,
                              String checkIn, String checkOut) {
        try {
            File folder = new File("receipts");
            if (!folder.exists()) folder.mkdir();

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File file = new File("receipts/receipt_" + username + "_" + timestamp + ".txt");
            try (FileWriter fw = new FileWriter(file)) {
                fw.write("------------------------------------------------------------\n");
                fw.write("                     KL HOTEL\n");
                fw.write("             Davao City, Philippines\n");
                fw.write("     Tel: 09061606041  Email: klhotel@gmail.com\n");
                fw.write("------------------------------------------------------------\n");
                fw.write("Date Issued: " + new SimpleDateFormat("MMMM dd, yyyy").format(new Date()) + "\n");
                fw.write("Guest Name: " + guestName + "\n");
                fw.write("Username: " + username + "\n");
                fw.write("Room Type: " + roomType + "\n");
                fw.write("Nights Stayed: " + days + "\n");
                fw.write("Check-In: " + checkIn + "\n");
                fw.write("Check-Out: " + checkOut + "\n");
                fw.write(String.format("Subtotal: P%.2f%n", subtotal));
                fw.write(String.format("Discount: %.0f%%%n", discount * 100));
                fw.write(String.format("Tax (12%%): P%.2f%n", tax));
                fw.write(String.format("TOTAL DUE: P%.2f%n", total));
                fw.write("------------------------------------------------------------\n");
                fw.write("    THANK YOU FOR STAYING AT KL HOTEL!\n");
                fw.write("------------------------------------------------------------\n");
            }
            System.out.println("\nReceipt saved successfully: " + file.getPath());
        } catch (IOException e) {
            System.out.println("Error saving receipt: " + e.getMessage());
        }
    }

    static void saveRoomAvailability() {
        try (FileWriter fw = new FileWriter("rooms.txt")) {
            for (int i = 0; i < roomAvailability.length; i++) {
                fw.write(roomTypes[i] + "," + roomPrices[i] + "," + roomAvailability[i] + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error saving room data: " + e.getMessage());
        }
    }

    static void loadRoomAvailability() {
        File file = new File("rooms.txt");
        if (!file.exists()) {
            saveRoomAvailability();
            return;
        }

        try (Scanner sc = new Scanner(file)) {
            int i = 0;
            while (sc.hasNextLine() && i < roomAvailability.length) {
                String[] data = sc.nextLine().split(",");
                if (data.length == 3) {
                    roomAvailability[i] = Integer.parseInt(data[2]);
                }
                i++;
            }
        } catch (IOException e) {
            System.out.println("Error loading room data: " + e.getMessage());
        }
    }
}
