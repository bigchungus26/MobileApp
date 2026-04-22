<?php
include 'connection.php';

$user_id = $_POST['user_id'];
$title = $_POST['title'];
$description = $_POST['description'];
$category = $_POST['category'];
$frequency = $_POST['frequency'];

$stmt = $conn->prepare("INSERT INTO habits (user_id, title, description, category, frequency) VALUES (?, ?, ?, ?, ?)");
$stmt->bind_param("issss", $user_id, $title, $description, $category, $frequency);

if ($stmt->execute()) {
    $habit_id = $stmt->insert_id;

    // Auto-create today's task for this habit
    $today = date('Y-m-d');
    $task_stmt = $conn->prepare("INSERT INTO tasks (user_id, habit_id, date) VALUES (?, ?, ?)");
    $task_stmt->bind_param("iis", $user_id, $habit_id, $today);
    $task_stmt->execute();
    $task_stmt->close();

    echo json_encode(array("success" => true, "message" => "Habit added"));
} else {
    echo json_encode(array("success" => false, "message" => "Failed to add habit"));
}

$stmt->close();
$conn->close();
?>
