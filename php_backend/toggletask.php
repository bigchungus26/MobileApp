<?php
include 'connection.php';

$task_id = $_POST['task_id'];
$user_id = $_POST['user_id'];

// Get current status
$stmt = $conn->prepare("SELECT completed FROM tasks WHERE id = ? AND user_id = ?");
$stmt->bind_param("ii", $task_id, $user_id);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    $row = $result->fetch_assoc();
    $new_status = $row['completed'] == 1 ? 0 : 1;
    $completed_at = $new_status == 1 ? date('Y-m-d H:i:s') : null;

    $update = $conn->prepare("UPDATE tasks SET completed = ?, completed_at = ? WHERE id = ?");
    $update->bind_param("isi", $new_status, $completed_at, $task_id);
    $update->execute();
    $update->close();

    echo json_encode(array("success" => true, "completed" => $new_status));
} else {
    echo json_encode(array("success" => false, "message" => "Task not found"));
}

$stmt->close();
$conn->close();
?>
