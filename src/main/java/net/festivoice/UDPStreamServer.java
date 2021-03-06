/*
 * festivoice
 *
 * Copyright 2009 FURUHASHI Sadayuki, KASHIHARA Shuzo, SHIBATA Yasuharu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package net.festivoice;

import java.lang.*;
import java.util.*;
import java.net.*;
import java.util.concurrent.*;

class UDPDataWithAddress
{
	private InetSocketAddress address;
	private UDPData data;

	public UDPDataWithAddress(DatagramPacket received) throws Exception
	{
		address = new InetSocketAddress(received.getAddress(), received.getPort());
		data = UDPData.deserialize(received.getData());
	}

	public InetSocketAddress getInetSocketAddress()
	{
		return address;
	}

	public UDPData getData()
	{
		return data;
	}

}

class Worker extends Thread
{
	private DatagramSocket socket;
	private IChannelManager channelServer;
	private BlockingQueue<UDPDataWithAddress> queue;
	private boolean endFlag = false;

	public Worker(DatagramSocket socket, IChannelManager channelServer, BlockingQueue<UDPDataWithAddress> queue)
	{
		this.socket = socket;
		this.channelServer = channelServer;
		this.queue = queue;
	}

	public void run()
	{
		while(!endFlag) {
			try {
				UDPDataWithAddress dataWithAddress = queue.take();
				InetSocketAddress fromAddress = dataWithAddress.getInetSocketAddress();

				UDPData data = dataWithAddress.getData();
				IChannelInfo channel = channelServer.channelData(data.getChannelName(),
						data.getUserName(), fromAddress);

				short userIndex = 0;
				for(IUserInfo user : channel.getUsers()) {
					if(user.getInetSocketAddress().equals(fromAddress)) {
						break;
					} else {
						++userIndex;
					}
				}

				for(IUserInfo user : channel.getUsers()) {
					if(!user.getInetSocketAddress().equals(fromAddress)) {
						byte[] send_data = UDPData.serialize(data.getUserName(),data.getChannelName(),data.getVoiceData(),
								data.getSequenceNumber(),userIndex);
						DatagramPacket send = new DatagramPacket(send_data, send_data.length, user.getInetSocketAddress());
						socket.send(send);
					}
				}
			} catch (Exception e) {
				//System.out.println("send error: "+e);
				//e.printStackTrace();
			}
		}
	}

	public void end()
	{
		endFlag = true;
	}
}

public class UDPStreamServer extends AbstractStreamServer {
	private static int DEFAULT_QUEUE_MAX = 2048;

	private DatagramSocket socket;
	private Worker[] workers;
	private BlockingQueue<UDPDataWithAddress> queue;

	UDPStreamServer(InetSocketAddress addr, IChannelManager channelServer) throws SocketException
	{
		socket = new DatagramSocket(addr);

		queue = new LinkedBlockingQueue<UDPDataWithAddress>(DEFAULT_QUEUE_MAX);

		workers = new Worker[1];
		for(int i=0; i < workers.length; ++i) {
			workers[i] = new Worker(socket, channelServer, queue);
		}
	}

	public void run()
	{
		for(Worker w : workers) {
			w.start();
		}
		byte[] buf = new byte[1024 * 35];
		DatagramPacket received = new DatagramPacket(buf, buf.length);
		received.setLength(buf.length);
		while (true) {
			try {
				socket.receive(received);

				UDPDataWithAddress data = new UDPDataWithAddress(received);
				queue.put(data);

			} catch (Exception e) {
			}
		}
	}

	private void changeQueueItems(int itemNumber)
	{
		BlockingQueue new_queue = new LinkedBlockingQueue<UDPDataWithAddress>(itemNumber);
		queue.drainTo(new_queue);
		queue = new_queue;
	}
}

