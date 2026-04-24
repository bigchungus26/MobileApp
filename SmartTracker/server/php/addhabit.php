<?php
require_once __DIR__ . '/db.php';

$userId      = isset($_POST['user_id'])     ? (int) $_POST['user_id']     : 0;
$title       = isset($_POST['title'])       ? trim($_POST['title'])       : '';
$description = isset($_POST['description']) ? trim($_POST['description']) : '';
$category    = isset($_POST['category'])    ? trim($_POST['category'])    : '';
$frequency   = isset($_POST['frequency'])   ? trim($_POST['frequency'])   : 'DAILY';

if ($userId <= 0 || $title === '') {
    http_response_code(400);
    echo 'Missing required fields';
    exit;
}

$stmt = $conn->prepare(
    "INSERT INTO habits (user_id, title, description, category, frequency)
     VALUES (?, ?, ?, ?, ?)"
);
$stmt->bind_param('issss', $userId, $title, $description, $category, $frequency);
$stmt->execute();

echo 'Habit added';
