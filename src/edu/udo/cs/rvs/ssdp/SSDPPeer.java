package edu.udo.cs.rvs.ssdp;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

/**
 * This class is first instantiated on program launch and IF (and only if) it
 * implements Runnable, a {@link Thread} is created and started.
 *
 */
public class SSDPPeer {
	//test
	static boolean Exit = true;

	public SSDPPeer() {
	}


	private static class TheListThread implements Runnable {
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
				//TODO do not forgot to change the Datagramminit trace() !!
				InputStream in = new ByteArrayInputStream(datagramPacket.getData());
				InputStreamReader inputStream = new InputStreamReader(in, StandardCharsets.UTF_8);
				reader = new BufferedReader(inputStream);
               //TODO we should add a methode to read the buffer , add the UUID , DienstTYPE , for the unicast it's found
				//TODO on the USN and ST , for multicast it's found on the USN and NT ,,, alive or dead on the last line for multicast
				// TODO add a List to store the UUID and Dienst-Type
			}
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

				}
				if (input.equals("LIST")) {

				}
				if (input.equals("SCAN")) {

				}


			}
		}
	}
}


