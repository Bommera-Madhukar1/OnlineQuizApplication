import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;

public class OnlineQuizApplication {
    private static HashMap<String, String> users = new HashMap<>(); // Username -> Password
    private static ArrayList<Quiz> quizzes = new ArrayList<>();
    private static HashMap<String, ArrayList<QuizAttempt>> userScores = new HashMap<>(); // Username -> Scores

    public static void main(String[] args) {
        // Prepopulate with test user
        users.put("admin", "admin123");

        // Add a quiz with 10 questions
        quizzes.add(new Quiz("General Knowledge", new Question[]{
            new Question("What is the capital of France?", new String[]{"Berlin", "Madrid", "Paris", "Rome"}, 2),
            new Question("Which planet is known as the Red Planet?", new String[]{"Earth", "Mars", "Jupiter", "Saturn"}, 1),
            new Question("Who wrote 'Hamlet'?", new String[]{"Shakespeare", "Dickens", "Hemingway", "Frost"}, 0),
            new Question("What is the boiling point of water?", new String[]{"90°C", "100°C", "80°C", "120°C"}, 1),
            new Question("What is the largest mammal?", new String[]{"Elephant", "Blue Whale", "Shark", "Giraffe"}, 1),
            new Question("Which is the smallest country in the world?", new String[]{"Monaco", "Vatican City", "Malta", "San Marino"}, 1),
            new Question("Who painted the Mona Lisa?", new String[]{"Michelangelo", "Van Gogh", "Leonardo da Vinci", "Picasso"}, 2),
            new Question("What is the speed of light?", new String[]{"300,000 km/s", "150,000 km/s", "1,000 km/s", "500,000 km/s"}, 0),
            new Question("Who discovered penicillin?", new String[]{"Alexander Fleming", "Marie Curie", "Albert Einstein", "Isaac Newton"}, 0),
            new Question("What is the square root of 144?", new String[]{"10", "11", "12", "14"}, 2)
        }));

        SwingUtilities.invokeLater(LoginScreen::new);
    }

    // Validate user credentials
    public static boolean isValidUser(String username, String password) {
        return users.containsKey(username) && users.get(username).equals(password);
    }

    // Register a new user
    public static boolean registerUser(String username, String password) {
        if (users.containsKey(username)) return false;
        users.put(username, password);
        return true;
    }

    // Get all available quizzes
    public static ArrayList<Quiz> getQuizzes() {
        return quizzes;
    }

    // Record the user's quiz attempt and score
    public static void recordQuizAttempt(String username, Quiz quiz, int score, ArrayList<String> incorrectQuestions) {
        userScores.putIfAbsent(username, new ArrayList<>());
        userScores.get(username).add(new QuizAttempt(quiz, score, incorrectQuestions));
    }

    // Get leaderboard, sorted by highest score
    public static ArrayList<QuizAttempt> getLeaderboard() {
        ArrayList<QuizAttempt> leaderboard = new ArrayList<>();
        userScores.values().forEach(leaderboard::addAll);
        leaderboard.sort((a, b) -> b.getScore() - a.getScore());
        return leaderboard;
    }

    // Login Screen
    static class LoginScreen extends JFrame {
        public LoginScreen() {
            setTitle("Online Quiz Application - Login");
            setSize(400, 300);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            getContentPane().setBackground(new Color(230, 240, 250));

            JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
            panel.setBackground(new Color(230, 240, 250));
            JLabel usernameLabel = new JLabel("Username:");
            JTextField usernameField = new JTextField();
            usernameField.setPreferredSize(new Dimension(15, 25)); // Adjusted size
           // usernameField.setTabSize(10);
            JLabel passwordLabel = new JLabel("Password:");
            JPasswordField passwordField = new JPasswordField();
            passwordField.setPreferredSize(new Dimension(150, 25)); // Adjusted size
            JButton loginButton = createStyledButton("Login", Color.BLUE, new Dimension(150, 40)); // Larger button
            JButton registerButton = createStyledButton("Register", Color.GREEN, new Dimension(150, 40)); // Larger button

            panel.add(usernameLabel);
            panel.add(usernameField);
            panel.add(passwordLabel);
            panel.add(passwordField);
            panel.add(loginButton);
            panel.add(registerButton);

            add(panel);

            loginButton.addActionListener(e -> {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                if (isValidUser(username, password)) {
                    JOptionPane.showMessageDialog(this, "Login Successful!");
                    SwingUtilities.invokeLater(() -> new QuizSelectionScreen(username));
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid credentials.");
                }
            });

            registerButton.addActionListener(e -> {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                if (registerUser(username, password)) {
                    JOptionPane.showMessageDialog(this, "Registration Successful!");
                } else {
                    JOptionPane.showMessageDialog(this, "Username already exists.");
                }
            });

            setVisible(true);
        }
    }

    // Quiz Selection Screen
    static class QuizSelectionScreen extends JFrame {
        public QuizSelectionScreen(String username) {
            setTitle("Select a Quiz");
            setSize(500, 400);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            getContentPane().setBackground(new Color(210, 230, 240));

            JLabel welcomeLabel = new JLabel("Welcome, " + username + "! Select a quiz or view leaderboard:", SwingConstants.CENTER);
            welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
            add(welcomeLabel, BorderLayout.NORTH);

            JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
            panel.setBackground(new Color(210, 230, 240));
            for (Quiz quiz : getQuizzes()) {
                JButton quizButton = createStyledButton(quiz.getTitle(), Color.MAGENTA, new Dimension(150, 40)); // Adjusted button size
                quizButton.addActionListener(e -> {
                    SwingUtilities.invokeLater(() -> new QuizScreen(username, quiz));
                    dispose();
                });
                panel.add(quizButton);
            }

            JButton leaderboardButton = createStyledButton("Leaderboard", Color.ORANGE, new Dimension(150, 40)); // Adjusted button size
            leaderboardButton.addActionListener(e -> SwingUtilities.invokeLater(LeaderboardScreen::new));
            panel.add(leaderboardButton);

            add(panel, BorderLayout.CENTER);
            setVisible(true);
        }
    }

    // Quiz Screen
    static class QuizScreen extends JFrame {
        private int currentQuestionIndex = 0;
        private int score = 0;
        private ArrayList<String> incorrectQuestions = new ArrayList<>();

        public QuizScreen(String username, Quiz quiz) {
            setTitle(quiz.getTitle());
            setSize(600, 400);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            getContentPane().setBackground(new Color(240, 250, 255));

            Question[] questions = quiz.getQuestions();
            JPanel panel = new JPanel(new BorderLayout());
            JLabel questionLabel = new JLabel("", SwingConstants.CENTER);
            questionLabel.setFont(new Font("Arial", Font.BOLD, 16));
            JPanel optionsPanel = new JPanel(new GridLayout(0, 1));
            optionsPanel.setBackground(new Color(240, 250, 255));
            ButtonGroup group = new ButtonGroup();
            JButton nextButton = createStyledButton("Next", Color.CYAN, new Dimension(180, 50)); // Larger button

            updateQuestion(questions[currentQuestionIndex], questionLabel, optionsPanel, group);

            nextButton.addActionListener(e -> {
                int selectedOption = getSelectedOption(group);
                if (selectedOption == questions[currentQuestionIndex].getCorrectOption()) {
                    score++;
                } else {
                    incorrectQuestions.add("Q: " + questions[currentQuestionIndex].getText() +
                            " Correct: " + questions[currentQuestionIndex].getOptions()[questions[currentQuestionIndex].getCorrectOption()]);
                }

                currentQuestionIndex++;
                if (currentQuestionIndex < questions.length) {
                    updateQuestion(questions[currentQuestionIndex], questionLabel, optionsPanel, group);
                } else {
                    recordQuizAttempt(username, quiz, score, incorrectQuestions);
                    JOptionPane.showMessageDialog(this, "Quiz Completed! Your score: " + score + "/" + questions.length);
                    SwingUtilities.invokeLater(() -> new ReviewScreen(incorrectQuestions));
                    dispose();
                }
            });

            panel.add(questionLabel, BorderLayout.NORTH);
            panel.add(optionsPanel, BorderLayout.CENTER);
            panel.add(nextButton, BorderLayout.SOUTH);

            add(panel);
            setVisible(true);
        }

        private void updateQuestion(Question question, JLabel questionLabel, JPanel optionsPanel, ButtonGroup group) {
            questionLabel.setText(question.getText());
            optionsPanel.removeAll();
            group.clearSelection();

            String[] options = question.getOptions();
            for (int i = 0; i < options.length; i++) {
                JRadioButton optionButton = new JRadioButton(options[i]);
                optionButton.setActionCommand(String.valueOf(i));
                group.add(optionButton);
                optionsPanel.add(optionButton);
            }
            optionsPanel.revalidate();
            optionsPanel.repaint();
        }

        private int getSelectedOption(ButtonGroup group) {
            String command = group.getSelection() == null ? "-1" : group.getSelection().getActionCommand();
            return Integer.parseInt(command);
        }
    }

    // Review Screen for Incorrect Questions
    static class ReviewScreen extends JFrame {
        public ReviewScreen(ArrayList<String> incorrectQuestions) {
            setTitle("Review Incorrect Answers");
            setSize(600, 400);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(new Color(240, 250, 255));

            JTextArea reviewArea = new JTextArea();
            reviewArea.setText(String.join("\n\n", incorrectQuestions));
            reviewArea.setEditable(false);
            reviewArea.setBackground(new Color(240, 250, 255));
            panel.add(new JScrollPane(reviewArea), BorderLayout.CENTER);

            JButton backButton = createStyledButton("Back to Quiz Selection", Color.PINK, new Dimension(180, 50));
            backButton.addActionListener(e -> {
                SwingUtilities.invokeLater(() -> new QuizSelectionScreen("user"));
                dispose();
            });

            panel.add(backButton, BorderLayout.SOUTH);
            add(panel);
            setVisible(true);
        }
    }

    // Leaderboard Screen
    static class LeaderboardScreen extends JFrame {
        public LeaderboardScreen() {
            setTitle("Leaderboard");
            setSize(500, 400);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(new Color(230, 240, 250));

            JTextArea leaderboardArea = new JTextArea();
            ArrayList<QuizAttempt> leaderboard = getLeaderboard();
            StringBuilder leaderboardText = new StringBuilder("Leaderboard:\n\n");

            for (QuizAttempt attempt : leaderboard) {
                leaderboardText.append(attempt.getQuiz().getTitle()).append(": ")
                        .append(attempt.getScore()).append("\n");
            }

            leaderboardArea.setText(leaderboardText.toString());
            leaderboardArea.setEditable(false);
            leaderboardArea.setBackground(new Color(230, 240, 250));
            panel.add(new JScrollPane(leaderboardArea), BorderLayout.CENTER);

            JButton backButton = createStyledButton("Back to Quiz Selection", Color.ORANGE, new Dimension(180, 50));
            backButton.addActionListener(e -> {
                SwingUtilities.invokeLater(() -> new QuizSelectionScreen("user"));
                dispose();
            });

            panel.add(backButton, BorderLayout.SOUTH);
            add(panel);
            setVisible(true);
        }
    }

    private static JButton createStyledButton(String text, Color color, Dimension size) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setPreferredSize(size);
        button.setBorder(BorderFactory.createLineBorder(color.darker()));
        return button;
    }
}

// Quiz, Question, and QuizAttempt Classes
class Quiz {
    private String title;
    private Question[] questions;

    public Quiz(String title, Question[] questions) {
        this.title = title;
        this.questions = questions;
    }

    public String getTitle() {
        return title;
    }

    public Question[] getQuestions() {
        return questions;
    }
}

class Question {
    private String text;
    private String[] options;
    private int correctOption;

    public Question(String text, String[] options, int correctOption) {
        this.text = text;
        this.options = options;
        this.correctOption = correctOption;
    }

    public String getText() {
        return text;
    }

    public String[] getOptions() {
        return options;
    }

    public int getCorrectOption() {
        return correctOption;
    }
}

class QuizAttempt {
    private Quiz quiz;
    private int score;
    private ArrayList<String> incorrectQuestions;

    public QuizAttempt(Quiz quiz, int score, ArrayList<String> incorrectQuestions) {
        this.quiz = quiz;
        this.score = score;
        this.incorrectQuestions = incorrectQuestions;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public int getScore() {
        return score;
    }

    public ArrayList<String> getIncorrectQuestions() {
        return incorrectQuestions;
    }
}