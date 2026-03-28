package projects;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class QuizApplicaton extends JFrame implements ActionListener {
    // Form fields
    JTextField nameField, emailField, mobileField, cityField, stateField, countryField;
    JLabel questionLabel;
    JRadioButton optionA, optionB, optionC, optionD;
    JButton nextButton, lifelineButton;
    ButtonGroup optionsGroup;

    // User details
    String name, email, city, state, country;
    long mobileNumber;

    // Quiz data
    int questionIndex = 0;
    int reward = 0;
    boolean lifelineUsed = false;
    boolean audienceUsed = false;
    boolean fiftyUsed = false;

    // ✅ JDBC Configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/quiz_user?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Deepu"; // Change this

    // Questions (question, A, B, C, D, correct)
    String[][] questions = {
        {"Who is known as the Father of Computers?", "Alan Turing", "Bill Gates", "Charles Babbage", "Steve Jobs", "C"},
        {"Which planet is known as the Red Planet?", "Earth", "Mars", "Venus", "Jupiter", "B"},
        {"What is the square root of 64?", "6", "7", "8", "9", "C"},
        {"How many continents are there on Earth?", "5", "6", "7", "8", "C"},
        {"Who wrote 'Hamlet'?", "Dickens", "Austen", "Shakespeare", "Orwell", "C"},
        {"Which ocean is the largest?", "Indian", "Pacific", "Atlantic", "Arctic", "B"},
        {"Who wrote the play Romeo and Juliet?", "Mark Twain", "Leo Tolstoy", "William Shakespeare", "Charles Dickens", "C"},
        {"Fastest land animal?", "Cheetah", "Lion", "Leopard", "Horse", "A"},
        {"Which is a programming language?", "Python", "Cobra", "Viper", "Snake", "A"},
        {"Which number is prime?", "15", "21", "29", "33", "C"}
    };

    int[] prize = {1000, 2000, 4000, 8000, 16000, 32000, 64000, 125000, 250000, 500000};

    // Constructor
    public QuizApplicaton() {
        setTitle("Quiz Application");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        showUserForm();
        applyTheme(getContentPane());
        setVisible(true);
    }

    // User form
    private void showUserForm() {
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        nameField = new JTextField();
        emailField = new JTextField();
        mobileField = new JTextField();
        cityField = new JTextField();
        stateField = new JTextField();
        countryField = new JTextField();

        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Mobile Number:"));
        formPanel.add(mobileField);
        formPanel.add(new JLabel("City:"));
        formPanel.add(cityField);
        formPanel.add(new JLabel("State:"));
        formPanel.add(stateField);
        formPanel.add(new JLabel("Country:"));
        formPanel.add(countryField);

        JButton startButton = new JButton("Start Quiz");
        startButton.addActionListener(e -> startQuiz());

        formPanel.add(new JLabel());
        formPanel.add(startButton);

        getContentPane().add(formPanel, BorderLayout.CENTER);
        applyTheme(formPanel);
    }

    // Start quiz
    private void startQuiz() {
        try {
            name = nameField.getText();
            email = emailField.getText();
            mobileNumber = Long.parseLong(mobileField.getText());
            city = cityField.getText();
            state = stateField.getText();
            country = countryField.getText();

            if (name.isEmpty() || email.isEmpty() || city.isEmpty() || state.isEmpty() || country.isEmpty()) {
                throw new Exception("Empty fields");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid input, please fill all fields properly.");
            return;
        }

        getContentPane().removeAll();

        questionLabel = new JLabel("", JLabel.CENTER);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 18));
        questionLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        optionA = new JRadioButton();
        optionB = new JRadioButton();
        optionC = new JRadioButton();
        optionD = new JRadioButton();
        optionsGroup = new ButtonGroup();
        optionsGroup.add(optionA);
        optionsGroup.add(optionB);
        optionsGroup.add(optionC);
        optionsGroup.add(optionD);

        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 40));
        optionsPanel.add(optionA);
        optionsPanel.add(optionB);
        optionsPanel.add(optionC);
        optionsPanel.add(optionD);

        nextButton = new JButton("Next");
        nextButton.addActionListener(this);

        lifelineButton = new JButton("Use Lifeline");
        lifelineButton.addActionListener(e -> useLifeline());

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(nextButton);
        bottomPanel.add(lifelineButton);

        getContentPane().add(questionLabel, BorderLayout.NORTH);
        getContentPane().add(optionsPanel, BorderLayout.CENTER);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        applyTheme(getContentPane());
        loadQuestion();
        revalidate();
        repaint();
    }

    // Load question
    private void loadQuestion() {
        if (questionIndex >= questions.length) {
            showFinalResult();
            return;
        }

        optionA.setVisible(true);
        optionB.setVisible(true);
        optionC.setVisible(true);
        optionD.setVisible(true);

        String[] q = questions[questionIndex];
        questionLabel.setText("Q" + (questionIndex + 1) + ": " + q[0]);
        optionA.setText("A. " + q[1]);
        optionB.setText("B. " + q[2]);
        optionC.setText("C. " + q[3]);
        optionD.setText("D. " + q[4]);

        optionsGroup.clearSelection();
    }

    // Action handler for next
    @Override
    public void actionPerformed(ActionEvent e) {
        String answer = "";
        if (optionA.isSelected()) answer = "A";
        else if (optionB.isSelected()) answer = "B";
        else if (optionC.isSelected()) answer = "C";
        else if (optionD.isSelected()) answer = "D";

        if (answer.equals("")) {
            JOptionPane.showMessageDialog(this, "Please select an option.");
            return;
        }

        if (answer.equals(questions[questionIndex][5])) {
            reward = prize[questionIndex];
            questionIndex++;
            loadQuestion();
        } else {
            JOptionPane.showMessageDialog(this, "❌ Wrong answer! Game Over.\nCorrect answer was: " + questions[questionIndex][5]);
            showFinalResult();
        }
    }

    // Lifeline logic
    private void useLifeline() {
        if (lifelineUsed) {
            JOptionPane.showMessageDialog(this, "You have already used a lifeline.");
            return;
        }

        String[] choices = new String[]{"Audience Poll", "50-50"};
        String lifeline = (String) JOptionPane.showInputDialog(this, "Choose a lifeline:", "Lifeline",
                JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);

        if (lifeline == null) return;
        lifelineUsed = true;

        if (lifeline.equals("Audience Poll") && !audienceUsed) {
            JOptionPane.showMessageDialog(this, "📊 Audience Poll:\n"
                    + optionA.getText() + " - 10%\n"
                    + optionB.getText() + " - 20%\n"
                    + optionC.getText() + " - 60%\n"
                    + optionD.getText() + " - 10%");
            audienceUsed = true;
        } else if (lifeline.equals("50-50") && !fiftyUsed) {
            String correct = questions[questionIndex][5];
            if (correct.equals("A")) {
                optionB.setVisible(false);
                optionD.setVisible(false);
            } else if (correct.equals("B")) {
                optionA.setVisible(false);
                optionC.setVisible(false);
            } else if (correct.equals("C")) {
                optionA.setVisible(false);
                optionD.setVisible(false);
            } else {
                optionB.setVisible(false);
                optionC.setVisible(false);
            }
            fiftyUsed = true;
        } else {
            JOptionPane.showMessageDialog(this, "This lifeline has already been used.");
        }
    }

    // Final result
    private void showFinalResult() {
        getContentPane().removeAll();

        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 16));
        resultArea.setForeground(Color.WHITE);
        resultArea.setBackground(new Color(0, 56, 64));

        resultArea.setText("🎉 Quiz Completed 🎉\n\n");
        resultArea.append("Name: " + name + "\n");
        resultArea.append("Email: " + email + "\n");
        resultArea.append("Mobile: " + mobileNumber + "\n");
        resultArea.append("City: " + city + "\n");
        resultArea.append("State: " + state + "\n");
        resultArea.append("Country: " + country + "\n");
        resultArea.append("\n🏆 Total Reward: ₹" + reward + "\n");

        getContentPane().add(new JScrollPane(resultArea), BorderLayout.CENTER);
        applyTheme(getContentPane());

        saveResultToDatabase();

        revalidate();
        repaint();
    }

    // Save result to DB
    private void saveResultToDatabase() {
        String insertQuery = "INSERT INTO quiz_results (username, email, mobile, city, state, country, reward) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = con.prepareStatement(insertQuery)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setLong(3, mobileNumber);
            ps.setString(4, city);
            ps.setString(5, state);
            ps.setString(6, country);
            ps.setInt(7, reward);

            ps.executeUpdate();
            System.out.println("✅ Result saved successfully in database.");

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving result to database: " + e.getMessage());
        }
    }

    // 🎨 Dark Teal + Gold Theme
    private void applyTheme(Component comp) {
        Color bg = new Color(0, 56, 64);
        Color panelBg = new Color(0, 70, 80);
        Color btn = new Color(0, 180, 216);
        Color txt = Color.WHITE;

        if (comp instanceof JPanel) {
            comp.setBackground(panelBg);
        } else if (comp instanceof JButton) {
            comp.setBackground(btn);
            comp.setForeground(Color.BLACK);
            ((JButton) comp).setFocusPainted(false);
            ((JButton) comp).setFont(new Font("Arial", Font.BOLD, 14));
        } else if (comp instanceof JLabel) {
            comp.setForeground(txt);
            ((JLabel) comp).setFont(new Font("Arial", Font.BOLD, 14));
        } else if (comp instanceof JTextField) {
            comp.setBackground(new Color(220, 220, 220));
            comp.setForeground(Color.BLACK);
            ((JTextField) comp).setFont(new Font("Arial", Font.PLAIN, 14));
        } else if (comp instanceof JRadioButton) {
            comp.setBackground(panelBg);
            comp.setForeground(txt);
            ((JRadioButton) comp).setFont(new Font("Arial", Font.PLAIN, 14));
        }

        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                applyTheme(child);
            }
        }
    }

    // Main
    public static void main(String[] args) {
        // Nimbus Look & Feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(QuizApplicaton::new);
    }
}
