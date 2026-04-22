<?php
include 'connection.php';

$user_id = $_POST['user_id'];
$title = $_POST['title'];
$duration_minutes = $_POST['duration_minutes'];
$calories = $_POST['calories'];
$intensity = $_POST['intensity'];
$date = date('Y-m-d');

$stmt = $conn->prepare("INSERT INTO workouts (user_id, title, duration_minutes, calories, intensity, date) VALUES (?, ?, ?, ?, ?, ?)");
$stmt->bind_param("isiiss", $user_id, $title, $duration_minutes, $calories, $intensity, $date);

if ($stmt->execute()) {
    echo json_encode(array("success" => true, "message" => "Workout added"));
} else {
    echo json_encode(array("success" => false, "message" => "Failed to add workout"));
}

$stmt->close();
$conn->close();
?>
