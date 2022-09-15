import java.util.ArrayList;
import java.util.List;

public class Test {
    public static List<String> fizzBuzz(int n) {
        List<String> answer = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            if (i % 3 == 0 && i % 5 == 0) {
                answer.add("FizzBuzz");
                continue;
            }
            if (i % 3 == 0) {
                answer.add("Fizz");
                continue;
            }
            if (i % 5 == 0) {
                answer.add("Buzz");
                continue;
            }
            answer.add(String.valueOf(i));
        }
        return answer;
    }

    public class ListNode {
        int val;
        ListNode next;

        ListNode() {
        }

        ListNode(int val) {
            this.val = val;
        }

        ListNode(int val, ListNode next) {
            this.val = val;
            this.next = next;
        }
    }

    public ListNode middleNode(ListNode head) {
        
        return null;
    }

    public static void main(String[] args) {
        System.out.println(fizzBuzz(15));
    }
}
