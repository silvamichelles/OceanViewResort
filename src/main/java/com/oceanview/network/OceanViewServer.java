package com.oceanview.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.oceanview.service.RoomService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OceanViewServer {

    private static final Logger LOGGER = Logger.getLogger(OceanViewServer.class.getName());
    private static final int PORT = 8080;
    private static final int THREAD_POOL_SIZE = 10;
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.setExecutor(Executors.newFixedThreadPool(THREAD_POOL_SIZE));

        registerHandlers(server);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down Ocean View server...");
            server.stop(5);
        }));

        server.start();
        LOGGER.info(String.format("🚀 Ocean View Web Service started on port %d", PORT));
        System.out.println("\n🌐 Guest Portal : http://localhost:8080/\n");
    }

    private static void registerHandlers(HttpServer server) {
        RoomService roomService = new RoomService();

        // Guest Portal (Root)
        server.createContext("/", new RootHandler());

        // APIs
        server.createContext("/api/rooms", exchange -> {
            addCorsHeaders(exchange);
            if (handlePreflight(exchange)) return;
            switch (exchange.getRequestMethod().toUpperCase()) {
                case "GET" -> handleRequest(exchange, () -> roomService.getAllRooms());
                default    -> sendError(exchange, 405, "Method Not Allowed");
            }
        });

        server.createContext("/api/auth/login", new AuthHandler());

        server.createContext("/health", exchange -> {
            addCorsHeaders(exchange);
            sendJson(exchange, 200, Map.of("status", "UP", "service", "OceanView"));
        });
    }

    // ─────────────────────────────────────────────────────────────
    //  1. GUEST PORTAL UI (Improved Premium Design)
    // ─────────────────────────────────────────────────────────────
    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String html = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Ocean View Resort | Premium Stays</title>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600&family=Playfair+Display:ital,wght@0,500;0,700;1,500&display=swap" rel="stylesheet">
    <style>
        :root {
            --primary: #0a192f;
            --secondary: #d4af37;
            --secondary-hover: #b5952f;
            --bg-light: #f8f9fa;
            --text-dark: #1f2937;
            --text-light: #6b7280;
            --white: #ffffff;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body {
            font-family: 'Montserrat', sans-serif;
            background-color: var(--bg-light);
            color: var(--text-dark);
            line-height: 1.6;
            overflow-x: hidden;
            display: flex;
            flex-direction: column;
            min-height: 100vh;
        }
        
        /* Navbar */
        nav {
            background: rgba(255, 255, 255, 0.98);
            padding: 1.2rem 5%;
            box-shadow: 0 4px 20px rgba(0,0,0,0.05);
            display: flex; justify-content: space-between; align-items: center;
            position: sticky; top: 0; z-index: 1000;
            backdrop-filter: blur(10px);
        }
        .logo { font-family: 'Playfair Display', serif; font-size: 2rem; color: var(--primary); font-weight: 700; letter-spacing: 1px; }
        .logo span { color: var(--secondary); }
        .nav-links a { margin-left: 30px; text-decoration: none; color: var(--text-dark); font-weight: 500; font-size: 0.9rem; text-transform: uppercase; letter-spacing: 1px; cursor: pointer; transition: color 0.3s; }
        .nav-links a:hover { color: var(--secondary); }
        
        .page { display: none; min-height: calc(100vh - 80px); animation: fadeIn 0.6s ease-out; }
        .page.active { display: block; }
        @keyframes fadeIn { from { opacity: 0; transform: translateY(15px); } to { opacity: 1; transform: translateY(0); } }
        
        /* Home / Login Page */
        #home-page {
            background: linear-gradient(rgba(10, 25, 47, 0.8), rgba(10, 25, 47, 0.6)), url('https://images.unsplash.com/photo-1542314831-c6a4d14d8373?q=80&w=2070&auto=format&fit=crop') no-repeat center center/cover;
            display: flex; align-items: center; justify-content: center;
        }
        .login-box {
            background: rgba(255, 255, 255, 0.98);
            padding: 3.5rem 3rem; border-radius: 8px;
            width: 100%; max-width: 420px; text-align: center;
            box-shadow: 0 25px 50px rgba(0,0,0,0.3);
        }
        .login-box h2 { font-family: 'Playfair Display', serif; margin-bottom: 0.5rem; font-size: 2.2rem; color: var(--primary); }
        .login-box p { color: var(--text-light); margin-bottom: 2.5rem; font-size: 0.95rem; }
        .input-group { margin-bottom: 1.5rem; text-align: left; position: relative; }
        .input-group label { display: block; margin-bottom: 0.5rem; font-weight: 500; font-size: 0.85rem; text-transform: uppercase; letter-spacing: 0.5px; color: var(--text-dark); }
        .input-group input { width: 100%; padding: 1rem; border: 1px solid #e5e7eb; border-radius: 4px; font-family: inherit; font-size: 1rem; transition: border-color 0.3s, box-shadow 0.3s; }
        .input-group input:focus { outline: none; border-color: var(--secondary); box-shadow: 0 0 0 3px rgba(212, 175, 55, 0.1); }
        
        .btn { width: 100%; padding: 1.1rem; border: none; border-radius: 4px; font-family: inherit; font-weight: 600; font-size: 0.95rem; text-transform: uppercase; letter-spacing: 1px; cursor: pointer; transition: all 0.3s ease; }
        .btn-primary { background: var(--secondary); color: var(--white); box-shadow: 0 4px 15px rgba(212, 175, 55, 0.3); }
        .btn-primary:hover { background: var(--secondary-hover); transform: translateY(-2px); box-shadow: 0 6px 20px rgba(212, 175, 55, 0.4); }
        .btn-outline { background: transparent; color: var(--text-light); border: 1px solid #d1d5db; margin-top: 1rem; }
        .btn-outline:hover { background: #f3f4f6; color: var(--text-dark); }
        
        /* Rooms Explorer Page */
        #rooms-page { padding: 5rem 5%; background-color: var(--bg-light); }
        .section-header { text-align: center; margin-bottom: 4rem; }
        .section-header h1 { font-family: 'Playfair Display', serif; font-size: 3rem; color: var(--primary); margin-bottom: 1rem; }
        .section-header p { color: var(--text-light); font-size: 1.1rem; max-width: 600px; margin: 0 auto; }
        
        .rooms-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 2.5rem; }
        .room-card { background: var(--white); border-radius: 8px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.06); transition: all 0.4s ease; display: flex; flex-direction: column; }
        .room-card:hover { transform: translateY(-10px); box-shadow: 0 20px 40px rgba(0,0,0,0.12); }
        .room-img-wrapper { position: relative; height: 240px; overflow: hidden; }
        .room-img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.5s ease; }
        .room-card:hover .room-img { transform: scale(1.05); }
        .room-badge { position: absolute; top: 15px; right: 15px; background: rgba(10, 25, 47, 0.85); color: var(--secondary); padding: 5px 15px; border-radius: 4px; font-size: 0.8rem; font-weight: 600; letter-spacing: 1px; backdrop-filter: blur(4px); }
        
        .room-info { padding: 2rem; flex-grow: 1; display: flex; flex-direction: column; }
        .room-info h3 { font-family: 'Playfair Display', serif; font-size: 1.6rem; color: var(--primary); margin-bottom: 0.5rem; }
        .room-price { color: var(--secondary); font-size: 1.3rem; font-weight: 600; margin-bottom: 1.5rem; display: block; }
        .room-meta { display: flex; justify-content: space-between; color: var(--text-light); font-size: 0.9rem; margin-bottom: 1.5rem; padding-bottom: 1.5rem; border-bottom: 1px solid #f3f4f6; }
        .room-info .btn-primary { margin-top: auto; }
        
        /* Checkout Page */
        #checkout-page { padding: 4rem 5%; display: flex; justify-content: center; align-items: flex-start; }
        .checkout-container { background: var(--white); padding: 3.5rem; border-radius: 8px; box-shadow: 0 15px 35px rgba(0,0,0,0.08); width: 100%; max-width: 650px; }
        .checkout-container h2 { font-family: 'Playfair Display', serif; margin-bottom: 2rem; color: var(--primary); font-size: 2.2rem; text-align: center; }
        
        .selected-room-banner { background: #f8fafc; padding: 1.5rem; border-left: 4px solid var(--secondary); margin-bottom: 2.5rem; border-radius: 0 8px 8px 0; display: flex; justify-content: space-between; align-items: center; }
        .selected-room-banner div:first-child h4 { color: var(--primary); font-size: 1.2rem; margin-bottom: 0.2rem; }
        .selected-room-banner div:first-child p { color: var(--text-light); font-size: 0.9rem; }
        .selected-room-banner .price { font-size: 1.4rem; font-weight: 600; color: var(--secondary); }
        
        .date-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem; }
        
        /* Notifications & Loader */
        #notification { position: fixed; top: 20px; right: 20px; padding: 15px 25px; border-radius: 4px; color: white; transform: translateX(150%); transition: transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275); z-index: 2000; font-weight: 500; font-size: 0.95rem; box-shadow: 0 5px 15px rgba(0,0,0,0.2); }
        .notify-success { background: #10b981; }
        .notify-error { background: #ef4444; }
        .show-notify { transform: translateX(0) !important; }
        
        .loader { border: 3px solid rgba(255,255,255,0.3); border-top: 3px solid var(--white); border-radius: 50%; width: 20px; height: 20px; animation: spin 1s linear infinite; display: none; margin: 0 auto; }
        @keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
        
        /* Footer */
        footer { background: var(--primary); color: rgba(255,255,255,0.7); text-align: center; padding: 2rem; font-size: 0.9rem; margin-top: auto; }
        
        @media (max-width: 768px) {
            .date-grid { grid-template-columns: 1fr; }
            .login-box, .checkout-container { padding: 2rem; }
            .section-header h1 { font-size: 2.2rem; }
        }
    </style>
</head>
<body>
    <div id="notification">Message</div>
    
    <nav>
        <div class="logo">Ocean<span>View</span></div>
        <div class="nav-links">
            <a onclick="navigateTo('home-page')" id="nav-home">Guest Login</a>
            <a onclick="navigateTo('rooms-page')" id="nav-rooms" style="display:none;">Discover Rooms</a>
            <a id="nav-logout" style="display:none;" onclick="logout()">Logout</a>
        </div>
    </nav>
    
    <div id="home-page" class="page active">
        <div class="login-box">
            <h2>Welcome Back</h2>
            <p>Sign in to manage your luxury stay</p>
            <form id="loginForm" onsubmit="handleLogin(event)">
                <div class="input-group">
                    <label>Username</label>
                    <input type="text" id="username" required placeholder="Enter your username">
                </div>
                <div class="input-group">
                    <label>Password</label>
                    <input type="password" id="password" required placeholder="••••••••">
                </div>
                <button type="submit" class="btn btn-primary">
                    <span id="login-text">Sign In</span>
                    <span class="loader" id="login-loader"></span>
                </button>
            </form>
            <button onclick="skipLogin()" class="btn btn-outline">Continue as Guest (Demo)</button>
        </div>
    </div>
    
    <div id="rooms-page" class="page">
        <div class="section-header">
            <h1>Exclusive Accommodations</h1>
            <p>Experience unparalleled comfort and breathtaking views in our carefully curated rooms and suites.</p>
        </div>
        <div class="rooms-grid" id="rooms-container">
            <div style="text-align:center; grid-column: 1/-1; padding: 3rem;">
                <div class="loader" style="display:inline-block; border-top-color:var(--primary); border-color:#e5e7eb;"></div>
                <p style="margin-top:1rem; color:var(--text-light);">Loading available rooms...</p>
            </div>
        </div>
    </div>
    
    <div id="checkout-page" class="page">
        <div class="checkout-container">
            <h2>Complete Reservation</h2>
            
            <div class="selected-room-banner" id="selected-room-display">
                </div>
            
            <form id="bookingForm" onsubmit="handleBooking(event)">
                <input type="hidden" id="book-roomId">
                <input type="hidden" id="book-roomPrice">
                <input type="hidden" id="book-roomType">
                
                <div class="input-group">
                    <label>Full Name</label>
                    <input type="text" id="book-name" required placeholder="e.g. John Doe">
                </div>
                <div class="input-group">
                    <label>Contact Number</label>
                    <input type="tel" id="book-contact" required placeholder="e.g. +94 77 123 4567">
                </div>
                <div class="date-grid">
                    <div class="input-group">
                        <label>Check-in Date</label>
                        <input type="date" id="book-checkin" required>
                    </div>
                    <div class="input-group">
                        <label>Check-out Date</label>
                        <input type="date" id="book-checkout" required>
                    </div>
                </div>
                <div style="display:flex; gap:1rem; margin-top: 1.5rem;">
                    <button type="button" class="btn btn-outline" style="margin-top:0; width:30%;" onclick="navigateTo('rooms-page')">Back</button>
                    <button type="submit" class="btn btn-primary" style="width:70%;">
                        <span id="book-text">Confirm & Download Receipt</span>
                        <span class="loader" id="book-loader"></span>
                    </button>
                </div>
            </form>
        </div>
    </div>
    
    <footer>
        <p>&copy; 2026 Ocean View Resort. All rights reserved.</p>
    </footer>

    <script>
        const API_BASE_URL = 'http://localhost:8080/api';
        let isLoggedIn = false;
        
        function navigateTo(pageId) {
            document.querySelectorAll('.page').forEach(page => page.classList.remove('active'));
            document.getElementById(pageId).classList.add('active');
            window.scrollTo({ top: 0, behavior: 'smooth' });
            if(pageId === 'rooms-page') fetchRooms();
        }
        
        function showNotification(msg, type = 'success') {
            const notif = document.getElementById('notification');
            notif.textContent = msg;
            notif.className = type === 'success' ? 'notify-success show-notify' : 'notify-error show-notify';
            setTimeout(() => { notif.classList.remove('show-notify'); }, 3500);
        }
        
        async function handleLogin(e) {
            e.preventDefault();
            const u = document.getElementById('username').value;
            const p = document.getElementById('password').value;
            
            document.getElementById('login-text').style.display = 'none';
            document.getElementById('login-loader').style.display = 'inline-block';
            
            try {
                const response = await fetch(`${API_BASE_URL}/auth/login`, {
                    method: 'POST', headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ username: u, password: p })
                });
                if (response.ok) {
                    isLoggedIn = true;
                    document.getElementById('nav-home').style.display = 'none';
                    document.getElementById('nav-rooms').style.display = 'inline';
                    document.getElementById('nav-logout').style.display = 'inline';
                    showNotification('Welcome back to Ocean View!');
                    navigateTo('rooms-page');
                } else {
                    showNotification('Incorrect username or password', 'error');
                }
            } catch (error) {
                showNotification('Connection failed. Server might be down.', 'error');
            } finally {
                document.getElementById('login-text').style.display = 'inline';
                document.getElementById('login-loader').style.display = 'none';
            }
        }
        
        function skipLogin() {
            isLoggedIn = true;
            document.getElementById('nav-home').style.display = 'none';
            document.getElementById('nav-rooms').style.display = 'inline';
            document.getElementById('nav-logout').style.display = 'inline';
            navigateTo('rooms-page');
            showNotification('Guest Demo Mode Activated');
        }
        
        function logout() {
            isLoggedIn = false;
            document.getElementById('nav-home').style.display = 'inline';
            document.getElementById('nav-rooms').style.display = 'none';
            document.getElementById('nav-logout').style.display = 'none';
            document.getElementById('loginForm').reset();
            navigateTo('home-page');
        }
        
        async function fetchRooms() {
            const container = document.getElementById('rooms-container');
            
            try {
                const response = await fetch(`${API_BASE_URL}/rooms`);
                if(response.ok) {
                    const rooms = await response.json();
                    renderRooms(rooms);
                } else throw new Error("Failed");
            } catch (error) {
                const fallbackRooms = [
                    { roomNumber: "101", type: "Single", price: 5000.0 },
                    { roomNumber: "102", type: "Double", price: 8500.0 },
                    { roomNumber: "201", type: "Deluxe", price: 15000.0 },
                    { roomNumber: "301", type: "Ocean View Suite", price: 25000.0 }
                ];
                renderRooms(fallbackRooms);
                showNotification('Showing offline availability data', 'error');
            }
        }
        
        function renderRooms(rooms) {
            const container = document.getElementById('rooms-container');
            container.innerHTML = '';
            
            const imgMap = {
                'Single': 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800&auto=format&fit=crop',
                'Double': 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&auto=format&fit=crop',
                'Deluxe': 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&auto=format&fit=crop',
                'Ocean View Suite': 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&auto=format&fit=crop'
            };
            
            rooms.forEach(room => {
                const img = imgMap[room.type] || imgMap['Single'];
                const card = document.createElement('div');
                card.className = 'room-card';
                card.innerHTML = `
                    <div class="room-img-wrapper">
                        <img src="${img}" class="room-img" alt="${room.type} Room">
                        <span class="room-badge">Room ${room.roomNumber}</span>
                    </div>
                    <div class="room-info">
                        <h3>${room.type}</h3>
                        <div class="room-meta">
                            <span>Up to 2 Guests</span>
                            <span>Free WiFi</span>
                        </div>
                        <span class="room-price">Rs. ${room.price.toLocaleString()} <span style="font-size:0.8rem; color:var(--text-light); font-weight:400;">/ night</span></span>
                        <button class="btn btn-primary" onclick="initiateBooking('${room.roomNumber}', '${room.type}', ${room.price})">Reserve Room</button>
                    </div>
                `;
                container.appendChild(card);
            });
        }
        
        function initiateBooking(roomId, type, price) {
            if(!isLoggedIn) { showNotification('Please sign in to reserve a room', 'error'); navigateTo('home-page'); return; }
            
            document.getElementById('book-roomId').value = roomId;
            document.getElementById('book-roomType').value = type;
            document.getElementById('book-roomPrice').value = price;
            
            document.getElementById('selected-room-display').innerHTML = `
                <div>
                    <h4>${type} Selection</h4>
                    <p>Room No: ${roomId}</p>
                </div>
                <div class="price">Rs. ${price.toLocaleString()}</div>
            `;
            
            const today = new Date().toISOString().split('T')[0];
            document.getElementById('book-checkin').min = today;
            document.getElementById('book-checkout').min = today;
            
            navigateTo('checkout-page');
        }
        
        function handleBooking(e) {
            e.preventDefault();
            const name = document.getElementById('book-name').value;
            const phone = document.getElementById('book-contact').value;
            const inDate = document.getElementById('book-checkin').value;
            const outDate = document.getElementById('book-checkout').value;
            const roomId = document.getElementById('book-roomId').value;
            const type = document.getElementById('book-roomType').value;
            const price = parseFloat(document.getElementById('book-roomPrice').value);
            
            if(new Date(outDate) <= new Date(inDate)) { showNotification('Check-out must be after check-in date!', 'error'); return; }
            
            const nights = Math.ceil((new Date(outDate) - new Date(inDate)) / (1000 * 60 * 60 * 24));
            const totalBill = nights * price;
            const resNumber = 'RES-' + Date.now().toString().slice(-6);
            
            document.getElementById('book-text').style.display = 'none';
            document.getElementById('book-loader').style.display = 'inline-block';
            
            setTimeout(() => {
                document.getElementById('book-text').style.display = 'inline';
                document.getElementById('book-loader').style.display = 'none';
                
                showNotification('Reservation Confirmed! Downloading receipt...');
                downloadReceipt(name, phone, resNumber, type, roomId, inDate, outDate, nights, totalBill);
                
                document.getElementById('bookingForm').reset();
                setTimeout(() => { navigateTo('rooms-page'); }, 1000);
            }, 1500);
        }
        
        function downloadReceipt(name, phone, resNo, roomType, roomId, inDate, outDate, nights, total) {
            const receiptContent = `
=========================================
      OCEAN VIEW RESORT - SRI LANKA
       BOOKING CONFIRMATION RECEIPT
=========================================

Date Generated : ${new Date().toLocaleString()}
Reservation No : ${resNo}

--- GUEST DETAILS ---
Name           : ${name}
Contact        : ${phone}

--- RESERVATION DETAILS ---
Room Type      : ${roomType}
Room Number    : ${roomId}
Check-In Date  : ${inDate}
Check-Out Date : ${outDate}
Total Nights   : ${nights}

--- BILLING ---
Rate per night : Rs. ${parseFloat(document.getElementById('book-roomPrice').value).toLocaleString()}
TOTAL AMOUNT   : Rs. ${total.toLocaleString()}

=========================================
 Thank you for choosing Ocean View Resort!
   Please present this receipt on arrival.
=========================================
            `;
            const blob = new Blob([receiptContent], { type: 'text/plain' });
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.style.display = 'none';
            a.href = url;
            a.download = `Reservation_${resNo}.txt`;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
        }
    </script>
</body>
</html>
""";
            sendResponse(t, html, "text/html; charset=UTF-8");
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  2. AUTH HANDLER
    // ─────────────────────────────────────────────────────────────
    static class AuthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if (handlePreflight(exchange)) return;
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                try {
                    Map<String, String> creds = MAPPER.readValue(exchange.getRequestBody(),
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>(){});
                    com.oceanview.dao.impl.UserDAOImpl userDAO = new com.oceanview.dao.impl.UserDAOImpl();
                    boolean isValid = userDAO.authenticate(creds.get("username"), creds.get("password"));
                    if (isValid) sendJson(exchange, 200, Map.of("message", "Login successful"));
                    else sendError(exchange, 401, "Invalid credentials");
                } catch (Exception e) { sendError(exchange, 500, "Server Error"); }
            } else { sendError(exchange, 405, "Method Not Allowed"); }
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────
    private static void sendResponse(HttpExchange t, String response, String contentType) throws IOException {
        byte[] bytes = response.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        t.getResponseHeaders().set("Content-Type", contentType);
        t.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = t.getResponseBody()) { os.write(bytes); }
    }

    private static <T> void handleRequest(HttpExchange exchange, ThrowingSupplier<T> supplier) {
        try { sendJson(exchange, 200, supplier.get()); }
        catch (Exception e) { sendError(exchange, 500, "Internal Server Error"); }
    }

    private static void sendJson(HttpExchange exchange, int statusCode, Object payload) {
        try {
            byte[] responseBytes = MAPPER.writeValueAsBytes(payload);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(responseBytes); }
        } catch (IOException e) { LOGGER.log(Level.SEVERE, "Failed response", e); }
    }

    private static void sendError(HttpExchange exchange, int statusCode, String message) {
        sendJson(exchange, statusCode, Map.of("error", message, "status", statusCode));
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static boolean handlePreflight(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }

    @FunctionalInterface interface ThrowingSupplier<T> { T get() throws Exception; }
}