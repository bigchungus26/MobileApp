<?php
include 'connection.php';

$email = $_POST['email'];
$password = $_POST['password'];

$stmt = $conn->prepare("SELECT id, name, email, password FROM users WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    $row = $result->fetch_assoc();
    if (password_verify($password, $row['password'])) {
        echo json_encode(array(
            "success" => true,
            "userId" => $row['id'],
            "name" => $row['name'],
            "email" => $row['email']
        ));
    } else {
        echo json_encode(array("success" => false, "message" => "Invalid password"));
    }
} else {
    echo json_encode(array("success" => false, "message" => "User not found"));
}

$stmt->close();
$conn->close();
?>
