import java.io.*;
import java.util.*;

public class mips {
    static Map<String, String> opcodes = getOpcodeMap();
    static Map<String, String> registers = getRegisterMap();
    static Map<String, String> hex = getHexMap();
    static int lineNumber = 0;

    static Map<String, String> getOpcodeMap() {
        Map<String, String> map = new HashMap<>();
        map.put("add", "0000");
        map.put("srl", "0001");
        map.put("ori", "0010");
        map.put("sll", "0011");
        map.put("nor", "0100");
        map.put("j", "0101");
        map.put("lw", "0110");
        map.put("beq", "0111");
        map.put("sub", "1000");
        map.put("subi", "1001");
        map.put("and", "1010");
        map.put("andi", "1011");
        map.put("or", "1100");
        map.put("sw", "1101");
        map.put("addi", "1110");
        map.put("bneq", "1111");
        return map;
    }

    static Map<String, String> getRegisterMap() {
        Map<String, String> map = new HashMap<>();
        map.put("$zero", "0000");
        map.put("$t0", "0001");
        map.put("$t1", "0010");
        map.put("$t2", "0011");
        map.put("$t3", "0100");
        map.put("$t4", "0101");
        return map;
    }

    static Map<String, String> getHexMap() {
        Map<String, String> map = new HashMap<>();
        map.put("0000", "0");
        map.put("0001", "1");
        map.put("0010", "2");
        map.put("0011", "3");
        map.put("0100", "4");
        map.put("0101", "5");
        map.put("0110", "6");
        map.put("0111", "7");
        map.put("1000", "8");
        map.put("1001", "9");
        map.put("1010", "a");
        map.put("1011", "b");
        map.put("1100", "c");
        map.put("1101", "d");
        map.put("1110", "e");
        map.put("1111", "f");
        return map;
    }

    static boolean isNotRType(String op) {
        return !(op.compareTo("add") == 0 || op.compareTo("sub") == 0 || op.compareTo("and") == 0 || op.compareTo("or") == 0
                || op.compareTo("nor") == 0);
    }

    static String hexCodeGenerator(String input) {
        String op, reg, srcRegister = "", dstRegister = "", src2Register = "", shiftAmount = "", immediate = "";
        String[] values = input.split(",");
        op = values[0].split(" ")[0].trim();
        String operation = opcodes.get(op);
        reg = values[0].split(" ")[1].trim();
        if (values.length == 3) {
            srcRegister = registers.get(reg.trim());
            if (isNotRType(op)) {
                dstRegister = registers.get(values[1].trim());
                if (op.compareTo("sll") == 0 || op.compareTo("srl") == 0) {
                    shiftAmount = values[2].trim();
                    int temp = Integer.parseInt(shiftAmount);
                    shiftAmount = Integer.toBinaryString(0b10000 | temp).substring(1);
                } else {
                    immediate = values[2].trim();
                    int temp = Integer.parseInt(immediate);
                    if (temp < 0) {
                        temp = 16 + temp;
                    }
                    immediate = Integer.toBinaryString(0b10000 | temp).substring(1);
                }
            } else {
                src2Register = registers.get(values[1].trim());
                dstRegister = registers.get(values[2].trim());
            }
        } else {
            if (op.equals("j")) {
                immediate = values[0].split(" ")[1].trim();
                int temp = Integer.parseInt(immediate);
                immediate = Integer.toBinaryString(0b100000000 | temp).substring(1);
                immediate += "0000";
            } else {
                reg = values[0].split(" ")[1].trim();
                if (op.equals("sw")) {
                    src2Register = registers.get(reg.trim());
                    String temp = values[1].substring(values[1].indexOf("$"), values[1].indexOf("$") + 3);
                    dstRegister = registers.get(temp.trim());
                } else {
                    srcRegister = registers.get(reg.trim());
                    String temp = values[1].substring(values[1].indexOf("$"), values[1].indexOf("$") + 3);
                    src2Register = registers.get(temp.trim());
                }
                String offset = values[1].trim().split("\\$")[0].substring(0, 1);
                int temp = Integer.parseInt(offset);
                immediate = Integer.toBinaryString(0b10000 | temp).substring(1);
            }
        }
        String finalCode = operation + srcRegister + src2Register + dstRegister +  shiftAmount + immediate;
        StringBuilder hexCode = new StringBuilder();
        for (int i = 0; i < 16; i += 4) {
            String temp = finalCode.substring(i, i + 4);
            hexCode.append(hex.get(temp));
        }
        return hexCode.toString();
    }

    public static void main(String[] args) throws IOException {
        String[] toWrite = new String[100];
        File inputFile = new File("input.txt");
        BufferedReader fileReader = new BufferedReader(new FileReader(inputFile));
        String input;
        Map<Integer, String> branchingStatements = new HashMap<>();
        Map<String, Integer> labels = new HashMap<>();
        String op, code;
        while ((input = fileReader.readLine()) != null) {
            if (input.length() < 1)
                continue;
            if (input.contains(":")) {
                String label = input.split(":")[0];
                labels.put(label, lineNumber);
                continue;
            }
            String[] values = input.split(",");
            op = values[0].split(" ")[0].trim();
            if (op.equals("j") || op.equals("beq") || op.equals("bneq")) {
                branchingStatements.put(lineNumber, input);
            } else {
                code = hexCodeGenerator(input);
                toWrite[lineNumber] = code;
            }
            lineNumber++;
        }
        for (Map.Entry<Integer,String> entry : branchingStatements.entrySet()) {
            String str = entry.getValue();
            int line1 = entry.getKey();
            String[] values = str.split(" ");
            op = values[0].split(" ")[0].trim();
            int line2;
            if (op.equals("j")) {
                line2 = labels.get(values[1]);
                int difference = line2;
                str = str.replace(values[1], Integer.toString(difference));
            } else {
                line2 = labels.get(values[3]);
                int difference = ((line2 - line1 > 0))? line2 - line1 - 1 : 16 + (line2 - line1) + 1;
                str = str.replace(values[3], Integer.toString(difference));
            }
            str = hexCodeGenerator(str);
            toWrite[line1] = str;
        }
        try {
            FileWriter fileWriter = new FileWriter("output.txt");
            fileWriter.write("v2.0 raw\n");
            for (int j = 0; j < lineNumber; j++) {
                fileWriter.write(toWrite[j] + " ");
                if (j != 0 && j % 8 == 0)
                    fileWriter.write("\n");
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
