<?php
$host   = 'localhost';
$user   = 'root';
$pass   = '';
$dbname = 'smarttracker';

$conn = new mysqli($host, $user, $pass, $dbname);

if ($conn->connect_error) {
    http_response_code(500);
    echo 'DB connection failed: ' . $conn->connect_error;
    exit;
}

$conn->set_charset('utf8mb4');
