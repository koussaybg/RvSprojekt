package edu.udo.cs.rvs.ssdp;


import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * This class is first instantiated on program launch and IF (and only if) it
 * implements Runnable, a {@link Thread} is created and started.
 *
 */
public class SSDPPeer {
	//test
	static boolean Exit = true;
	TheListThread h=new TheListThread() ;
	public SSDPPeer( )
	{
		TheListThread h=new TheListThread() ;
		Thread r=new Thread(h) ;
		r.start();

	}




	private static class TheListThread implements Runnable {
		private TheListThread() {

		}
		static final TheListThread instance = new TheListThread();

		LinkedList<DatagramPacket> datagramList = new LinkedList<>();

		@Override
		public void run() {
			try {
				byte[] buffer = new byte[1024];
				MulticastSocket multicastSocket = new MulticastSocket(1900);
				InetAddress inetAddress = InetAddress.getByName("239.255.255.250");
				multicastSocket.joinGroup(inetAddress);

				while (Exit) {
					DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
					multicastSocket.receive(datagramPacket);
					datagramList.add(datagramPacket);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * return the DatagramList
		 *
		 * @return List of Sockets
		 */
		LinkedList<DatagramPacket> getDatagramList() {
			return datagramList;
		}

		/**
		 * Singelton usage
		 *
		 * @return the instance of excuted List
		 */
		public static TheListThread getInstance() {
			return instance;
		}

	}

	private static class TheWorkerThread implements Runnable {
		static final TheWorkerThread instance = new TheWorkerThread();
		BufferedReader reader;

        /**
         * Methode uded to Read the DatagramPacket , store it or Delete it if the device is dead
         * @param rd the Reader
         * @param devices List of devices to store or to to delete from it
         * @throws IOException if an error in buffer occur
         */
        void Read_store_Delete(Reader rd , LinkedList<Device> devices) throws IOException {
        	boolean alive=true ;
			String buffer;
			String[] memory;
			BufferedReader in = new BufferedReader(rd);
			Device device = new Device();
			while ((buffer = in.readLine()) != null)
			{
				memory = buffer.split(":", 2);
				if (memory[0].equals("ST") || memory[0].equals("NT")) {
					device.addDevices(memory[1]);
					continue;
				}
				if (memory[0].equals("USN")) {
					memory = memory[1].split(":");
					device.setUuid(memory[1]);
				}
				if (memory[0].equals("NTS")) {
					if (memory[1].equals("„ssdp:byebye")) {
						for (Device dv : devices) {
							if (dv.equals(device)) {
								devices.remove(dv);
								alive=false ;
							}
						}
					}
				}
			}
			if(alive)
			{
				devices.add(device) ;
			}
		}
		LinkedList<Device> AliveDevices = new LinkedList<>() ;
		@Override
		public void run() {
			while (Exit) {
				byte[] buffer = new byte[1024];
				DatagramPacket datagramPacket;
				/**
				 * init the received datagramPacket
				 */
				while ((datagramPacket = TheListThread.getInstance().getDatagramList().pollFirst()) == null) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				InputStream in = new ByteArrayInputStream(datagramPacket.getData());
				InputStreamReader inputStream = new InputStreamReader(in, StandardCharsets.UTF_8);
				reader = new BufferedReader(inputStream);
				try {
					Read_store_Delete(reader, AliveDevices) ;
				} catch (IOException e) {
					e.printStackTrace();
				}


				//TODO we should add a methode to read the buffer , add the UUID , DienstTYPE , for the unicast it's found
				//TODO on the USN and ST , for multicast it's found on the USN and NT ,,, alive or dead on the last line for multicast
				// TODO add a List to store the UUID and Dienst-Type
			}

		}

		public LinkedList<Device> getAliveDevices() {
			return AliveDevices;
		}

		BufferedReader getBuffer() {
			return reader;
		}

		/**
		 * init singeltone
		 *
		 * @return the instance of the excuted thread
		 */
		public static TheWorkerThread getInstance() {
			return instance;
		}

	}

	private static class theUserThread implements Runnable {
		String input;

		@Override
		public void run() {
			while (Exit) {
				List<Device> alivelist ;
				alivelist =TheWorkerThread.getInstance().getAliveDevices() ;
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
				try {
					input = bufferedReader.readLine();
				} catch (IOException e) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
					continue;
				}
				if (input.equals("EXIT")) {
					Exit = false;
					break;
				}
				if (input.equals("CLEAR")) {
					TheWorkerThread.getInstance().getAliveDevices().clear();
					System.out.println(" Devices Cleared successfully");
				}
				if (input.equals("LIST")) {
					Print_Devices(alivelist)  ;

				}
				if (input.equals("SCAN")) {
					final int host =1900 ;
					String stringBuilder = "M-SEARCH * HTTP/1.1\n\r" +
							String.format("S: uuid:%s\n\r", UUID.randomUUID().toString()) +
							String.format("HOST: %d\n\r", host) +
							"MAN: \"ssdp:discover\" \n\r" +
							"ST: ssdp:all \n\r";
					byte[] bytes= stringBuilder.getBytes() ;
					InetAddress inetAddress= null;
					try {
						inetAddress = InetAddress.getByName("239.255.255.250");
					} catch (UnknownHostException e) {
						e.printStackTrace();
					}
					DatagramPacket datagramPacket=new DatagramPacket(bytes ,bytes.length,inetAddress,1900) ;
					try {
						MulticastSocket multicastSocket = new MulticastSocket();
						multicastSocket.send(datagramPacket);
					} catch (IOException e) {
						e.printStackTrace();
 					}
                //TODO add the output of the system

				}


			}
		}

        /**
         * Methode used to Print all device Type and their UUID
         * @param alivelist the list of all alive List
         */
		private void Print_Devices(List<Device> alivelist) {
			for (Device dv : alivelist) {
			 System.out.printf("%s - %s\n",dv.getUuid(),dv.getDevices());
			}

		}
	}

    /**
     * a private class to manage a new Data type named "device"
     */
	private static class Device  {
		String uuid  ;
		String devices  ;
		Device() { } ;

		 void addDevices(String device) {
			this.devices=device ;
		}

		 void setUuid(String uui) {
			this.uuid = uui;
		}

		public String getUuid() {
			return uuid;
		}

		public String getDevices() {
			return devices;
		}

        /**
         * an equal method
         * @param obj
         * @return
         */
		public boolean equals(Device obj) {
			return (this.getDevices().equals(obj.getDevices())&&this.getUuid().equals(obj.getUuid())) ;
		}
	}
}


