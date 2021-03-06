
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Rajith Radhakrishnan 
 * 109061463 
 * rajith.radhakrishnan@stonybrook.edu 
 * HW#4 CSE 214- R06 
 * TA - Frank Migliorino 
 * GA - Yu Wang
 *
 * @author radra_000
 */

/**
 * This is the main class of this programming assignment. When the program is 
 * run, the user will be able to enter details regarding the simulation such as the 
 * total number of intermediate routers to be used, arrival probability of the packet
 * to the dispactcher, bufferSize of the router(the total number packets it can 
 * process at the given time instant), minimum size and the maximum size of a packet
 * bandwidth size of the destination router, and the total simulation duration. 
 * The GUI is done in a separate SimulatorGUI class. 
 * 
 */
public class Simulator {

    /**
     *The max_packet is the total number of objects that can be created by 
     * this class at a given simulation instant. This is a final variable, which
     * means its a constant for all the various simulation runs. The destination 
     * count is the variable to keep track of the number of packets that has reached
     * the destination. The maxBufferSize, packetDropped, totalPacketsArrived, 
     * totalSeriveTime variables had to made static since some of them are  
     * being used both in this class and the Router class, and it wont changed 
     * for different instances. We also make the routers variable static since it
     * will be used both by the main method and also by the other methods of this 
     * class. 
     */
    public static final int MAX_PACKETS = 3;
    private static ArrayList<Router> routers = new ArrayList<>();
    private int destinationCount = 0;
    public static int maxBufferSize = 0, packetsDropped = 0, totalPacketsArrived = 0,
            totalSeriveTime = 0;
    private static ArrayList<Integer> priorityQueue = new ArrayList<>();
    Router dispatcher = new Router();

    /**
     *Main method that provides user with an interface to enter details about the 
     * simulation. We get the detail about the number of intermediate routers, 
     * arrival probability, max and min packet size, bandwidth, duration from the user
     * This program will run in a continuous loop until the user decides to stop it.
     * if an illegal value is entered for a certain variable, the program will 
     * ask the user to enter value again until the value is right. 
     * @param args
     *      arguments that will be entered if the program is ran using the command
     *      line
     */
        public static void main(String[] args) {
        double arrivalProb;
        int numIntRouters;
        int minPacketSize;
        int maxPacketSize;
        int bandwidth;
        int duration;
        int interRouter;
        Scanner input = new Scanner(System.in);
        boolean run = true;
        while (run) {
            routers.clear();
            priorityQueue.clear();
            Packet.setPacketCount(0);
            packetsDropped = 0;
            totalPacketsArrived = 0;
            totalSeriveTime = 0;
            System.out.println("Stating Simulator....");
            System.out.println("Enter the number of Intermediate router: ");
            interRouter = input.nextInt();
            while(interRouter < 1) {
                System.out.println("The Intermediate routers should be greater than 0");
                interRouter = input.nextInt();
            }

            System.out.println("Enter the arrival probability of a packet: ");
            arrivalProb = input.nextDouble();
            while (arrivalProb > 1) {
                System.out.println("The probabilty should be between 0 and 1");
                arrivalProb = input.nextDouble();
            }

            System.out.println("Enter the maximum buffer size of a router: ");
            maxBufferSize = input.nextInt();
            while(maxBufferSize < 1) {
                System.out.println("The buffersize has to be greater than 1");
                maxBufferSize = input.nextInt();
            }
            System.out.println("Enter the minimum size of a packet: ");
            minPacketSize = input.nextInt();
            while (minPacketSize < 0) {
                System.out.println("The minimum packet size "
                        + "should be greater than 1");
                minPacketSize = input.nextInt();
            }
            System.out.println("Enter the maximum size of a packet: ");
            maxPacketSize = input.nextInt();
            while (minPacketSize > maxPacketSize) {
                System.out.println("The max packet size should "
                        + "be greater than minimum packet size");
                maxPacketSize = input.nextInt();
            }
            System.out.println("Enter the Bandwidth size: ");
            bandwidth = input.nextInt();
            while (bandwidth < 1) {
                System.out.println("The bandwidth should be greater than 1");
                bandwidth = input.nextInt();
            }
            System.out.println("Enter the simlulation duration ");
            duration = input.nextInt();
            while (duration < 0) {
                System.out.println("The duration has to be greater than 0");
                duration = input.nextInt();
            }
            double result;
            Simulator simulator = new Simulator();
            for (int i = 0; i < interRouter; i++) {
                routers.add(new Router());//creting intermediate routers
                priorityQueue.add(i);
            }
            result = simulator.simulate(minPacketSize, maxPacketSize, duration, arrivalProb, bandwidth);
            System.out.println("Simluation ending.....");
            System.out.println("Total Service Time: " + totalSeriveTime);
            System.out.println("Total Packets Served: " + totalPacketsArrived);
            System.out.println("Average Service time per packet: "+ String.format("%.2f", result));
            System.out.println("Packets dropped: " + packetsDropped);
            System.out.println("\n\nDo you want to run the simulation again(y/n)");
            switch (input.next().toLowerCase()) {
                case ("y"):
                    run = true;
                    break;
                case ("n"):
                    run = false;
                    break;
                default:
                    System.out.println("NOT A VALID INPUT...PROGRAM EXITING");
                    run = false;
            }

        }
    }
    
    /**
     *simulate method performs all the function that are necessary to run the simulation
     * Depending on the arrival probability entered by the user, this method 
     * will create packets object with a random size value assined using the randInt 
     * method. and then it will add the packet to the dispatcher and output the 
     * progress to the user. and the creation count is incremented. this variable
     * is used to account for the cases where there is no packets created, and the
     * user should be notified appropriately. and then for every packet object 
     * that is placed in the dispatcher queue, the sendTo method in the router class
     * is called to send the packets to the intermediate routers.The processCheck
     * method is called to see if any routers have finished processing the packet
     * and are ready to send it to the destination. After printing out the
     * detail of the routers queue, the timeToDest from each packets in top of 
     * the queue in each router is subtracted. 
     * @param min
     *      minimum size of the packet, to set the lower limit of the random number
     *      generation. this value is obtained from the user. 
     * @param max
     *      maximum size of the packet, to set the upper limit of the random number
     *      generation . 
     * @param duration
     *      duration is the value of the total simulation time that is to be performed
     * @param prob
     *      prob is the value of the arrival probability of a packet into the dispatcher
     *      this value is entered by the user.
     * @param bandwidth
     *      bandwidth is the total number of packets that the destination will accept
     *      from the intermediate routers at a given, simulation time instant. 
     * @return
     *      double value of the average time it took to process the packets
     *      that reached the destination. 
     */
    public double simulate(int min, int max, int duration, double prob, int bandwidth) {
        int time = 1;
        while (time <= duration) {
            System.out.println("Time: "+time);
            destinationCount = 0; //all the packets in the destination should be cleared
            int creationCount = 0;
            for (int i = 1; i <= MAX_PACKETS; i++) {
                if (Math.random() < prob) {//checking if the packet is created
                    int size = randInt(min, max);
                    Packet packet = new Packet(size / 10, time);
                    dispatcher.enqueue(packet);
                    System.out.println("Packet " + Packet.packetCount + " arrives at the dispatcher"
                            + " with size " + size);
                    creationCount++;
                }
            }
            System.out.print((creationCount==0)?"No Packets created\n":"");
            int queueSize = dispatcher.size();
            for (int i = 0; i < queueSize; i++) {
                Packet packet = dispatcher.dequeue();
                int routerNo = Router.sendPacketTo(routers, packet);
                if(routerNo!=-1){
                System.out.println("Packet " + packet.getId() + " sent to Router " + routerNo);
                }
            }
            processingCheck(time, bandwidth);
            for (int i = 0; i < routers.size(); i++) {
                System.out.println("R" + (i+1) + ": " + routers.get(i).toString()+"}");
            }
            subtractTime();
            time++;
        }
        return (double)totalSeriveTime / totalPacketsArrived;
    }
    /**
     * subtractTime method will subtract the timeToDestination variable
     * of each packet that is on top of the queue on each intermediate routers
     * This function will not return any variable, since the routers 
     * is a static collection object. 
     */
    private void subtractTime(){
        for(int i = 0; i < routers.size(); i++){
            if(!routers.get(i).isEmpty()){
                Packet packet = routers.get(i).peek();
                packet.setTimeToDest(packet.getTimeToDest() - 1);
            }
        }
    }
    /**
     * processingCheck method will check each intermediate routers to see if 
     * there is any packet ready to be sent to the destination. That is the 
     * packet's timeToDestination should be zero and also the destination should 
     * have packets fewer than the bandwidth size. This method does not return 
     * anything. 
     * 
     * The algorithm employed here would send the packet from a routers, based
     * on fairness. that is it will keep a priority queue kind of algorithm
     * which would remove the router number from the queue, and place it on end of 
     * the queue, that way the routers that didnt get a chance will have a chance. 
     * @param time
     *      This is the current simulation time passed in from the simulate method
     *      This int value will be used to see how long a packet has stayed in
     *      the intermediate routers since it arrived there. 
     * @param bandwidth 
     *      The bandwidth is the int value that was passed in by the user, and 
     *      passed in again from the simulate method. 
     */
    private void processingCheck(int time, int bandwidth) {
        //int destinationCount = 0; 
        Packet packet;
        int priorityCount = 0;
        for (int i = 0; i < priorityQueue.size(); i++) {
            if (!routers.get(priorityQueue.get(i)).isEmpty()) {
                packet = routers.get(i).peek();
                if (packet.getTimeToDest() == 0) {
                    if (destinationCount < bandwidth) {
                        acceptAtDest(packet, time);
                        routers.get(i).dequeue();
                        destinationCount++;
                    } else {//algorithm to be fair to all intermediate routers
                        priorityQueue.remove(priorityQueue.indexOf(i));
                        priorityQueue.add(priorityCount, i);
                        priorityCount++;
                    }
                }
            }
        }
    }
    /**
     * acceptAtDest method will act like the destination, and accept the 
     * packets that are send in and prints the output,with information about
     * how long it took and what increment the totalSerivceTime static variable. 
     * @param packet
     *      packet that has been done processing will be sent to the destination
     *      its of the type Packet. 
     * @param time 
     *      The time parameter is the current simulation time, so the total time
     *      a packet spent in the intermediate routers can be calculated.
     */
    private void acceptAtDest(Packet packet, int time) {
        System.out.println("Packet " + packet.getId() + " has sucessfully"
                + " reached its destination: +" + (time - packet.getTimeArrive()));
        totalSeriveTime += (time - packet.getTimeArrive());
        totalPacketsArrived++;
    }
    /**
     * This method is a random integer generator,this method was provided to us
     * in the homework assignment guide. The method, will take in a minimum and 
     * maximum value, and return a integer value that is between these two limits
     * @param min
     *      min parameter is the minimum size for the packet to be created. 
     * @param max
     *      max parameter is the maximum size for the packet to be created. 
     * @return
     *      The integer value that is a natural number between the limits that 
     *      was passed in 
     * @throws IllegalArgumentException 
     *      exception is thrown when the max value is less than the minimum values.
     *      Although we do the check during the UI, this is just added to 
     *      make sure if there was another class creating an instance of this
     *      class, it would not return an error.
     */
    private int randInt(int min, int max) throws IllegalArgumentException {
        if (max < min) {
            throw new IllegalArgumentException("The Max value should be "
                    + "greater than Min value");
        } else {
            return (int) (Math.random() * ((max + 1) - min) + min);
        }
    }
}
