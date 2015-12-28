package com.thumbtack.hw;
/**
 * Created by adi on 12/23/15.
 */

import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.logging.Logger;

public class SimpleDB {
    private static final Logger LOGGER = Logger.getLogger(SimpleDB.class.getSimpleName());
    private static final Map<String, String> MAIN_DB = new HashMap<>(); // Main database.
    private static final Deque<HashMap<String, String>> TXNS = new LinkedList<>(); // Live transactions - using linked list since it reduces memory usage
    private static final Map<String, Integer> INV_IDX = new HashMap<>(); // Inverted index (sort of)
    private static final Deque<HashMap<String, Integer>> INV_IDX_TXNS = new LinkedList<>(); // Live transactions for numEqualTo




    class Elem implements Comparable<Elem>{
        public int a;
        Elem (int b) {
            a = b;
        }

        @Override
        public int compareTo (Elem o) {
            String s = String.valueOf(o.a);
            if (s.length() == String.valueOf(a).length()) {
                return Integer.compare(o.a, a);
            } else if (s.length() > String.valueOf(a).length()) {
                return 1;
            } else return -1;
        }
    }

    public String largestNumber(final List<Integer> a) {
        if (a == null || a.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();

        Map<Character, PriorityQueue<Elem>> m = new HashMap<Character, PriorityQueue<Elem>>();

        for (int i : a) {
            char c = String.valueOf(i).charAt(0);
            if (m.containsKey(c)) {
                PriorityQueue<Elem> q = m.get(c);
                q.add(new Elem(i));
            } else {
                PriorityQueue<Elem> q = new PriorityQueue<>();
                q.add(new Elem(i));
                m.put(c, q);
            }
        }

        for (char c = '9'; c >= '0'; c--) {
            if (m.containsKey(c)) {
                PriorityQueue<Elem> q = m.get(c);
                for (Elem e : q) {
                    sb.append(e.a);
                }
            }
        }

        String ret = sb.toString();
        if (ret.charAt(0) == '0') {
           ret = ret.replaceFirst("0", " ");
        }

        for (int i = 1; i < ret.length(); i++) {
            if (ret.charAt(i) == '0' && ret.charAt(i-1) == ' ') {
                ret = ret.replaceFirst("0", " ");
            }
            else break;
        }
        ret = ret.trim();
        ret = ret.equals("") ? "0" : ret;
        return ret;
    }





    public static void main(String[] args) {
        SimpleDB s = new SimpleDB();
        s.largestNumber(Arrays.asList(new Integer [] {0,0,0,0,0}));
        /*Scanner sc = new Scanner(System.in);
        BufferedReader reader = null;
        String readLine = null;
        if (args.length > 1 && args[0].equals("-f")) { // file mode
            try {
                reader = new BufferedReader(new FileReader(args[1]));
                readLine = reader.readLine();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        } else readLine = sc.nextLine(); // interactive mode
        try {
            while (readLine != null) {
                String[] arr = readLine.split(" ");
                switch (arr[0]) {
                    case "GET":
                        if (arr.length < 2) {
                            System.out.println("Usage: GET <key>");
                            break;
                        }
                        System.out.println(get(arr[1]));
                        break;
                    case "SET":
                        if (arr.length < 3) {
                            System.out.println("Usage: SET <key> <value>");
                            break;
                        }
                        set(arr[1], arr[2]);
                        break;
                    case "NUMEQUALTO":
                        if (arr.length < 2) {
                            System.out.println("Usage: NUMEQUALTO <key>");
                            break;
                        }
                        System.out.println(numEqualTo(arr[1]));
                        break;
                    case "UNSET":
                        if (arr.length < 2) {
                            System.out.println("Usage: UNSET <key>");
                            break;
                        }
                        unset(arr[1]);
                        break;
                    case "BEGIN":
                        INV_IDX_TXNS.push(new HashMap<>(16));
                        TXNS.push(new HashMap<>(16)); //keeping the initial capacity low - will be automatically doubled if needed
                        break;
                    case "ROLLBACK":
                        System.out.print(rollback());
                        break;
                    case "COMMIT":
                        System.out.print(commit());
                        break;
                    case "END":
                        System.exit(0);
                    default:
                        System.out.println("Invalid command : " + arr[0]);
                }
                if (reader == null) readLine = sc.nextLine(); //interactive mode
                else readLine = reader.readLine(); // file mode
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }*/
    }

    private static String get(String key) {
        if (!TXNS.isEmpty() && TXNS.peek().containsKey(key)) return TXNS.peek().get(key);
        return MAIN_DB.get(key);
    }

    private static void set(String key, String value) {
        if (!TXNS.isEmpty()) {
            String old = TXNS.peek().get(key);
            TXNS.peek().put(key, value);
            if (INV_IDX_TXNS.peek().containsKey(old)) INV_IDX_TXNS.peek().put(old, INV_IDX_TXNS.peek().get(old) - 1);
            if (INV_IDX_TXNS.peek().containsKey(value)) INV_IDX_TXNS.peek().put(value, INV_IDX_TXNS.peek().get(value) + 1);
            else INV_IDX_TXNS.peek().put(value, 1);
        }
        else {
            String old = MAIN_DB.get(key);
            MAIN_DB.put(key, value);
            if (INV_IDX.containsKey(old)) INV_IDX.put(old, INV_IDX.get(old) - 1);
            if (INV_IDX.containsKey(value)) INV_IDX.put(value, INV_IDX.get(value) + 1);
            else INV_IDX.put(value, 1);
        }
    }

    private static int numEqualTo(String key) {
        Integer ret = 0;
        if (!INV_IDX_TXNS.isEmpty() && INV_IDX_TXNS.peek().containsKey(key)) ret += INV_IDX_TXNS.peek().get(key);
        ret += INV_IDX.get(key) == null ? 0 : INV_IDX.get(key);
        return ret;
    }

    private static void unset(String key) {
        if (!TXNS.isEmpty()) {
            String value = TXNS.peek().remove(key);
            if (value == null) { //trying to unset a variable not set in this txn - adding a special marker.
                TXNS.peek().put(key, null); //special marker is null
                Integer curr = INV_IDX_TXNS.peek().get(MAIN_DB.get(key));
                INV_IDX_TXNS.peek().put(MAIN_DB.get(key), curr == null ? -1 : curr - 1);
            } else INV_IDX_TXNS.peek().put(value, INV_IDX_TXNS.peek().get(value) - 1);
        } else {
            String value = MAIN_DB.get(key);
            MAIN_DB.remove(key);
            INV_IDX.put(value, INV_IDX.get(value) - 1);
        }
    }

    private static String rollback() {
        if (TXNS.isEmpty()) return "NO TRANSACTION \n";
        else {
            TXNS.pop();
            INV_IDX_TXNS.pop();
        }
        return "";
    }

    private static String commit() {
        if (TXNS.isEmpty()) return "NO TRANSACTION \n";
        else {
            while (!TXNS.isEmpty()) {
                Map<String, String> temp = TXNS.removeLast(); // apply the first txn
                for (Map.Entry<String, String> entry : temp.entrySet()) {
                    String key = entry.getKey();
                    String val = entry.getValue();
                    if (val == null) { //marker case - remove from MAIN_DB and update INV_IDX
                        MAIN_DB.remove(key);
                    } else MAIN_DB.put(key, val);
                }
            }
            while (!INV_IDX_TXNS.isEmpty()) {
                Map<String, Integer> temp = INV_IDX_TXNS.removeLast(); // apply the first txn
                for (Map.Entry<String, Integer> entry : temp.entrySet()) {
                    String key = entry.getKey();
                    INV_IDX.put(key, (INV_IDX.get(key) == null ? 0 : INV_IDX.get(key)) + entry.getValue());
                }
            }
        }
        return "";
    }
}
