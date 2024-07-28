import javax.swing.*;
import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

public class SystemInfoGUI extends JFrame {

    private JLabel cpuLabel;
    private JLabel ramLabel;
    private JLabel gpuLabel;
    private JProgressBar cpuUsageBar;
    private JButton refreshButton;

    public SystemInfoGUI() {
        setTitle("System Information");
        setSize(600, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cpuLabel = new JLabel("CPU: ");
        ramLabel = new JLabel("RAM: ");
        gpuLabel = new JLabel("GPU: ");
        cpuUsageBar = new JProgressBar(0, 100);
        cpuUsageBar.setStringPainted(true);
        refreshButton = new JButton("Refresh");

        refreshButton.addActionListener(e -> fetchSystemInfo());

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(4, 1, 10, 10));
        infoPanel.add(cpuLabel);
        infoPanel.add(ramLabel);
        infoPanel.add(gpuLabel);
        infoPanel.add(new JLabel("CPU Usage:"));
        infoPanel.add(cpuUsageBar);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.add(infoPanel, BorderLayout.CENTER);
        mainPanel.add(refreshButton, BorderLayout.SOUTH);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(mainPanel);
        
        fetchSystemInfo();
        startCpuUsageMonitoring();
    }

    private void fetchSystemInfo() {
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("win")) {
                cpuLabel.setText("CPU: " + getWindowsCPUInfo());
                ramLabel.setText("RAM: " + getWindowsRAMInfo() + " MB");
                gpuLabel.setText("GPU: " + getWindowsGPUInfo());
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                cpuLabel.setText("CPU: " + getLinuxCPUInfo());
                ramLabel.setText("RAM: " + getLinuxRAMInfo() + " MB");
                gpuLabel.setText("GPU: " + getLinuxGPUInfo());
            } else {
                cpuLabel.setText("Unsupported OS");
                ramLabel.setText("Unsupported OS");
                gpuLabel.setText("Unsupported OS");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startCpuUsageMonitoring() {
        Timer timer = new Timer(true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                String os = System.getProperty("os.name").toLowerCase();
                try {
                    int usage = os.contains("win") ? getWindowsCPUUsage() : getLinuxCPUUsage();
                    SwingUtilities.invokeLater(() -> cpuUsageBar.setValue(usage));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, 1000); // Update every second
    }

    private String getWindowsCPUInfo() throws Exception {
        String command = "wmic cpu get name";
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        StringBuilder result = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty() && !line.contains("Name")) {
                result.append(line.trim());
            }
        }
        reader.close();
        return result.toString();
    }

    private long getWindowsRAMInfo() throws Exception {
        String command = "wmic memorychip get capacity";
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        long totalMemory = 0;
        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty() && !line.contains("Capacity")) {
                totalMemory += Long.parseLong(line.trim());
            }
        }
        reader.close();
        return totalMemory / (1024 * 1024); // Convert to MB
    }

    private String getWindowsGPUInfo() throws Exception {
        String command = "wmic path win32_videocontroller get name";
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        StringBuilder result = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty() && !line.contains("Name")) {
                result.append(line.trim());
            }
        }
        reader.close();
        return result.toString();
    }

    private int getWindowsCPUUsage() throws Exception {
        String command = "wmic cpu get loadpercentage";
        @SuppressWarnings("deprecation")
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        int usage = 0;
        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty() && !line.contains("LoadPercentage")) {
                usage = Integer.parseInt(line.trim());
            }
        }
        reader.close();
        return usage;
    }

    private String getLinuxCPUInfo() throws Exception {
        String[] command = {"/bin/sh", "-c", "lscpu | grep 'Model name'"};
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        StringBuilder result = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty()) {
                result.append(line.split(":")[1].trim());
            }
        }
        reader.close();
        return result.toString();
    }

    private long getLinuxRAMInfo() throws Exception {
        String command = "cat /proc/meminfo | grep MemTotal";
        @SuppressWarnings("deprecation")
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        long totalMemory = 0;
        if ((line = reader.readLine()) != null) {
            totalMemory = Long.parseLong(line.replaceAll("\\D+", ""));
        }
        reader.close();
        return totalMemory / 1024; // Convert to MB
    }

    private String getLinuxGPUInfo() throws Exception {
        String[] command = {"/bin/sh", "-c", "lspci | grep 'VGA'"};
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        StringBuilder result = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty()) {
                result.append(line.split(":")[2].trim());
            }
        }
        reader.close();
        return result.toString();
    }

    private int getLinuxCPUUsage() throws Exception {
        String[] command = {"/bin/sh", "-c", "top -bn1 | grep 'Cpu(s)' | " +
                "sed 's/.*, *\\([0-9.]*\\)%* id.*/\\1/' | " +
                "awk '{print 100 - $1}'"};
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        int usage = 0;
        if ((line = reader.readLine()) != null) {
            usage = (int) Double.parseDouble(line.trim());
        }
        reader.close();
        return usage;
    }

    public static void main(String[] args) {
        // FlatLaf temasını ayarlayın
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // GUI'yi başlat
        SwingUtilities.invokeLater(() -> {
            SystemInfoGUI frame = new SystemInfoGUI();
            frame.setVisible(true);
        });
    }
}
