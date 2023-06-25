package project21;

import java.util.concurrent.*;
import java.util.*;


class  Toast{
  public enum Status { DRY, CHEESE, SAUSAGE }
  private Status status = Status.DRY;
  private final int id;
  public Toast(int idn) { id = idn; }
  public void cheese() { status = Status.CHEESE; }
  public void sausage() { status = Status.SAUSAGE; }
  public Status getStatus() { return status; }
  public int getId() { return id; }
  public String toString() {
    return "Toast " + id + ": " + status;
  }
}

class ToastQueue extends LinkedBlockingQueue<Toast> {}

class Toaster implements Runnable {
  private ToastQueue toastQueue;
  private int count = 0;
  private Random rand = new Random(47);
  public Toaster(ToastQueue tq) { toastQueue = tq; }
  public void run() {
    try {
      while(!Thread.interrupted()) {
        TimeUnit.MILLISECONDS.sleep(
          100 + rand.nextInt(500));
        Toast t = new Toast(count++);
       System.out.println(t);
       toastQueue.put(t);
      }
    } catch(InterruptedException e) {
System.out.println("Toaster interrupted");
    }
    System.out.println("Toaster off");
  }
}


class PutCheese implements Runnable {
  private ToastQueue dryQueue, putCheeseQueue;
  public PutCheese(ToastQueue dry, ToastQueue putCheese) {
    dryQueue = dry;
    putCheeseQueue = putCheese;
  }
  public void run() {
    try {
      while(!Thread.interrupted()) {
        Toast t = dryQueue.take();
        t.cheese();
        System.out.println(t);
        putCheeseQueue.put(t);
      }
    } catch(InterruptedException e) {
    	System.out.println("Put Cheese interrupted");
    }
    System.out.println("Put Cheese off");
  }
}


class PutSausage implements Runnable {
  private ToastQueue putSausageQueue, finishedQueue;
  public PutSausage(ToastQueue putSausage, ToastQueue finished) {
    putSausageQueue = putSausage;
    finishedQueue = finished;
  }
  public void run() {
    try {
      while(!Thread.interrupted()) {
        Toast t = putSausageQueue.take();
        t.sausage();
        System.out.println(t);
        finishedQueue.put(t);
      }
    } catch(InterruptedException e) {
    	System.out.println("Put Sausage interrupted");
    }
    System.out.println("Put Sausage off");
  }
}
class Sandwich {
	private Toast top, bottom;
	private final int id;
	public Sandwich(Toast top, Toast bottom, int id) {
		this.top = top;
		this.bottom = bottom;
		this.id = id;
	}
	public int getId() {
		return id;
	}
	public Toast getTop() { return top; }
	public Toast getBottom() { return bottom; }
	public String toString() {
		return "Sandwich " + id + ": top: " + top + " and bottom: " + bottom;
	}
}

class SandwichQueue extends LinkedBlockingQueue<Sandwich> {}

class SandwichMaker implements Runnable {
	private int count = 0;
	private ToastQueue putCheeseQueue, putSausageQueue;
	private SandwichQueue sandwichQueue;
	public SandwichMaker(ToastQueue putCheese, ToastQueue putSausage, SandwichQueue sq) {
		putCheeseQueue = putCheese;
		putSausageQueue = putSausage;
		sandwichQueue = sq;
	}
	public void run() {
		try {
			while(!Thread.interrupted()) {
				Sandwich s = new Sandwich(
					putCheeseQueue.take(), putSausageQueue.take(), count++);
				System.out.println(s);
				sandwichQueue.put(s);
			}
		} catch(InterruptedException e) {
			System.out.println("SandwichMaker interrupted");
		}
		System.out.println("Sandwich maker off");
	}
}


class SandwichEater implements Runnable {
	private SandwichQueue sandwichQueue;
	private int counter = 0;
	public SandwichEater(SandwichQueue sq) {
		sandwichQueue = sq;
	}
	public void run() {
		try {
			while(!Thread.interrupted()) {
				Sandwich s = sandwichQueue.take();
				if(s.getId() != counter++ || 
					s.getTop().getStatus() != Toast.Status.CHEESE || 
					s.getBottom().getStatus() != Toast.Status.SAUSAGE) {
					System.out.println(">>>> Error: " + s);
						System.exit(1);
				} else
					System.out.println("NumNum! " + s);
			} 
		} catch(InterruptedException e) {
			System.out.println("SandwichEater interruped");
		}
		System.out.println("SandwichEater off");
	}
}

public class Toasted {
  public static void main(String[] args) throws Exception {
    ToastQueue dryQueue = new ToastQueue(),
               putCheeseQueue = new ToastQueue(),
               putSausageQueue = new ToastQueue();
    SandwichQueue sandwichQueue=new SandwichQueue();
    ExecutorService exec = Executors.newCachedThreadPool();
    exec.execute(new Toaster(dryQueue));
    exec.execute(new PutCheese(dryQueue, putCheeseQueue));
    exec.execute(new PutSausage(dryQueue, putSausageQueue));
    exec.execute(new SandwichMaker(putCheeseQueue,putSausageQueue,sandwichQueue));
    exec.execute(new SandwichEater(sandwichQueue));
    TimeUnit.SECONDS.sleep(5);
    exec.shutdownNow();
  }
} 