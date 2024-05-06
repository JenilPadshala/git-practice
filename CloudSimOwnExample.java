package org.cloudbus.cloudsim.examples;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;


public class CloudSimOwnExample {
	private static List<Vm> vmList;
	private static List<Cloudlet> cloudletList;
	public static void main(String[] args) {
		Log.printLine("Starting Cloudsim...");
		try {
			
			//Step 1: Initialize CloudSim package
			int num_user=1;
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;
			
			CloudSim.init(num_user, calendar, trace_flag);
			
			//Step 2: Create a datacenter
			Datacenter datacenter0 = createDatacenter("Datacenter_0");
			
			//Step 3: create a broker
			DatacenterBroker broker = new DatacenterBroker("Broker");
			int brokerId = broker.getId();
			
			//Step 4: create vms
			vmList = new ArrayList<Vm>();
			
			//description
			int vmid = 0;
			int mips = 50;
			long size = 10000;
			int ram = 1024;
			int bw = 512;
			int pesNumber = 1;
			String vmm = "Xen";
			
			//create 2 vms
			Vm vm1 = new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
			vmid++;
			Vm vm2 = new Vm(vmid, brokerId, mips, pesNumber, ram , bw, size, vmm, new CloudletSchedulerTimeShared());
			
			//add vm to vmList
			vmList.add(vm1);
			vmList.add(vm2);
			
			//submit the vmlist to broker;
			broker.submitVmList(vmList);
			
			//Step 5: create cloudlets
			cloudletList = new ArrayList<Cloudlet>();
			Random random = new Random();
			Scanner scanner = new Scanner(System.in); 
			Log.printLine("Enter number of cloudlets: "); 
			int n = scanner.nextInt();
			
			for (int i=0; i<n; i++) {
				int id = i;
				long length = random.nextInt(40000 - 20000 + 1) + 20000;
				long fileSize = random.nextInt(500 - 200 + 1) + 200;
				long outputSize = random.nextInt(500 - 200 + 1) + 200;
				UtilizationModel utilizationModel = new UtilizationModelFull();
				
				Cloudlet cloudlet = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
				cloudlet.setUserId(brokerId);
				cloudlet.setVmId(i%2);
				cloudletList.add(cloudlet);
				
			}
			
			broker.submitCloudletList(cloudletList);
			
			CloudSim.startSimulation();
			
			// Step 6: Print result
			List<Cloudlet> newList = broker.getCloudletReceivedList();
			CloudSim.stopSimulation();
			printCloudletList(newList);
			Log.printLine("CloudSim finished!");
			
		}catch(Exception e) {
			e.printStackTrace();
			Log.printLine("An error occurred!");
		}
}

//	private static DatacenterBroker createBroker() {
//		DatacenterBroker broker = null;
//		
//	}

	private static Datacenter createDatacenter(String name) {
		//1. create hostlist
		List<Host> hostList = new ArrayList<Host>();
		//2. Pe List
		List<Pe> peList = new ArrayList<Pe>();
		
		int mips = 100;
		//3. create pe
		peList.add(new Pe(0, new PeProvisionerSimple(mips)));
		
		//create a host
		int hostId = 0;
		int ram = 2048;
		long storage = 1000000;
		int bw = 512;
		
		hostList.add(new Host(
				hostId,
				new RamProvisionerSimple(ram),
				new BwProvisionerSimple(bw),
				storage,
				peList,
				new VmSchedulerTimeShared(peList)
				));
		
		//create a datacenter
		String arch = "x86";
		String os = "Linux";
		String vmm = "Xen";
		double time_zone = 10.0;
		double cost = 3.0;
		double costPerMem = 0.05;
		double costPerStorage = 0.001;
		double costPerBw = 0.0;
		
		LinkedList<Storage> storageList = new LinkedList<Storage>();
		
		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);
		
		
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return datacenter;
	}
	
	
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
				+ "Data center ID" + indent + "VM ID" + indent + "Time" + indent
				+ "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");

				Log.printLine(indent + indent + cloudlet.getResourceId()
						+ indent + indent + indent + cloudlet.getVmId()
						+ indent + indent
						+ dft.format(cloudlet.getActualCPUTime()) + indent
						+ indent + dft.format(cloudlet.getExecStartTime())
						+ indent + indent
						+ dft.format(cloudlet.getFinishTime()));
			}
		}
	}
}
