package it.polimi.middleware.kafka;

import java.io.*;

public class TraceBuilder {

    private static boolean valid(double x, double y, double min_x, double max_x, double min_y, double max_y) {
        if (x < min_x || x > max_x)
            return true;
        return y < min_y || y > max_y;
    }

    public static void main(String[] args) throws IOException {

        final boolean movingSensor = true;

        final String outputFilePath = "src/main/resources/";

        final int n_values = 1000;

        final double min_x = 0.0;
        final double max_x = 60.0;
        final double min_y = 0.0;
        final double max_y = 80.0;

        for (int i = 11; i <= 20; i++) {

            String outputFileName;

            if (movingSensor)
                outputFileName = "m_";
            else
                outputFileName = "f_";

            outputFileName += "trace" + i + ".txt";

            final double min_val = Math.random() * 70.0 - 10.0;
            final double max_val = Math.random() * 70.0 + 60.0;

            final double speed = Math.random() * 0.9 + 0.1;
            final double change_dir_probability = Math.random() * 0.5;

            System.out.println("File name: " + outputFileName);
            System.out.println("Noise interval: [" + ((float) min_val) + " dB, " + ((float) max_val) + " dB]");
            if (movingSensor) {
                System.out.println("Speed: " + ((float) speed) + " units per step");
                System.out.println("Probability of changing direction: " + (((float) change_dir_probability) * 100) + "%");
            }
            System.out.println();

            BufferedWriter out = new BufferedWriter(new FileWriter(outputFilePath + outputFileName));
            String record;

            double x = Math.random() * (max_x - min_x) - min_x;
            double y = Math.random() * (max_y - min_y) - min_y;
            double dir = Math.random() * 2 * Math.PI;
            double val;

            if (movingSensor) out.write("0");
            else out.write((((float) x) + " " + ((float) y)).replace('.', ','));
            out.newLine();

            for (int j = 0; j < n_values; j++) {

                if (Math.random() < change_dir_probability)
                    dir = Math.random() * 2 * Math.PI;

                x += Math.cos(dir) * speed;
                if (x > max_x)
                    x -= (max_x - min_x);
                else if (x < min_x)
                    x += (max_x - min_x);

                y += Math.sin(dir) * speed;
                if (y > max_y)
                    y -= (max_y - min_y);
                else if (y < min_y)
                    y += (max_y - min_y);

                val = (Math.random() * (max_val - min_val)) + min_val;

                if (valid(x, y, 2.8, 22.6, 32.0, 54.1) &&
                        valid(x, y, 26.1, 40.3, 3.1, 18.0)) {

                    if (movingSensor)
                        record = ((float) val) + " " + ((float) x) + " " + ((float) y);
                    else
                        record = ((float) val) + "";

                    out.write(record.replace('.', ','));
                    out.newLine();

                } else j--;
            }
            out.close();
        }
    }
}
