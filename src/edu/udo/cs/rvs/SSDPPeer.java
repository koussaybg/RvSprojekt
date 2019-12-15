package edu.udo.cs.rvs.ssdp;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.LinkedList;

/**
 * This class is first instantiated on program launch and IF (and only if) it
 * implements Runnable, a {@link Thread} is created and started.
 *
 */
public class SSDPPeer {
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

				while (!Exit) {
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


	private class TheWorkerThread implements Runnable {

		@Override
		public void run() {
			while (Exit) {
				byte[] buffer = new byte[1024];
				DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
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

			}
		}


	}
}
