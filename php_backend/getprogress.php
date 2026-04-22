<?php
include 'connection.php';

$user_id = $_GET['user_id'];
$today = date('Y-m-d');

// Today's habits
$stmt = $conn->prepare("SELECT COUNT(*) as total FROM tasks t JOIN habits h ON t.habit_id = h.id WHERE t.user_id = ? AND t.date = ? AND h.active = 1");
$stmt->bind_param("is", $user_id, $today);
$stmt->execute();
$habits_total = $stmt->get_result()->fetch_assoc()['total'];
$stmt->close();

$stmt = $conn->prepare("SELECT COUNT(*) as done FROM tasks t JOIN habits h ON t.habit_id = h.id WHERE t.user_id = ? AND t.date = ? AND t.completed = 1 AND h.active = 1");
$stmt->bind_param("is", $user_id, $today);
$stmt->execute();
$habits_done = $stmt->get_result()->fetch_assoc()['done'];
$stmt->close();

// Today's workouts
$stmt = $conn->prepare("SELECT COUNT(*) as total FROM workouts WHERE user_id = ? AND date = ?");
$stmt->bind_param("is", $user_id, $today);
$stmt->execute();
$workouts_total = $stmt->get_result()->fetch_assoc()['total'];
$stmt->close();

$stmt = $conn->prepare("SELECT COUNT(*) as done FROM workouts WHERE user_id = ? AND date = ? AND completed = 1");
$stmt->bind_param("is", $user_id, $today);
$stmt->execute();
$workouts_done = $stmt->get_result()->fetch_assoc()['done'];
$stmt->close();

// Weekly progress
$monday = date('Y-m-d', strtotime('monday this week'));
$daily = array();
$week_total = 0;
$week_done = 0;

for ($i = 0; $i < 7; $i++) {
    $day = date('Y-m-d', strtotime($monday . " + $i days"));
    if ($day > $today) break;

    $stmt = $conn->prepare("SELECT COUNT(*) as total FROM tasks t JOIN habits h ON t.habit_id = h.id WHERE t.user_id = ? AND t.date = ? AND h.active = 1");
    $stmt->bind_param("is", $user_id, $day);
    $stmt->execute();
    $dt = $stmt->get_result()->fetch_assoc()['total'];
    $stmt->close();

    $stmt = $conn->prepare("SELECT COUNT(*) as done FROM tasks t JOIN habits h ON t.habit_id = h.id WHERE t.user_id = ? AND t.date = ? AND t.completed = 1 AND h.active = 1");
    $stmt->bind_param("is", $user_id, $day);
    $stmt->execute();
    $dd = $stmt->get_result()->fetch_assoc()['done'];
    $stmt->close();

    $stmt = $conn->prepare("SELECT COUNT(*) as total FROM workouts WHERE user_id = ? AND date = ?");
    $stmt->bind_param("is", $user_id, $day);
    $stmt->execute();
    $wt = $stmt->get_result()->fetch_assoc()['total'];
    $stmt->close();

    $stmt = $conn->prepare("SELECT COUNT(*) as done FROM workouts WHERE user_id = ? AND date = ? AND completed = 1");
    $stmt->bind_param("is", $user_id, $day);
    $stmt->execute();
    $wd = $stmt->get_result()->fetch_assoc()['done'];
    $stmt->close();

    $total = $dt + $wt;
    $done = $dd + $wd;
    $week_total += $total;
    $week_done += $done;

    $day_name = date('l', strtotime($day));
    $pct = $total > 0 ? round(($done / $total) * 100, 1) : 0;
    $daily[$day_name] = $pct;
}

$weekly_pct = $week_total > 0 ? round(($week_done / $week_total) * 100, 1) : 0;

echo json_encode(array(
    "habitsCompleted" => intval($habits_done),
    "habitsTotal" => intval($habits_total),
    "workoutsCompleted" => intval($workouts_done),
    "workoutsTotal" => intval($workouts_total),
    "weeklyProgressPercent" => $weekly_pct,
    "dailyProgress" => $daily
));

$conn->close();
?>
