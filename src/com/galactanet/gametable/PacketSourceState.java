/*
* PacketManager.java: GameTable is in the Public Domain.
* 
* The purpose of this class is to let the app know where the state changes
* are coming from. Changes could come from loading a file, from the network,
* Or from user activity. 
*/

package com.galactanet.gametable;

/**
* 
* @author sephalon
*/


public class PacketSourceState
{
	// prevent instantiation
	private PacketSourceState()
	{
		throw new RuntimeException("PacketManager should not be instantiated!");
	}
	
	// HOST DUMP
	public static void beginHostDump()
	{
		g_bInitalHostDumpInProgress = true;
	}
	
	public static void endHostDump()
	{
		g_bInitalHostDumpInProgress = false;
	}
	
	public static boolean isHostDumping()
	{
		return g_bInitalHostDumpInProgress;
	}
	
	// FILE LOAD
	public static void beginFileLoad()
	{
		g_bPrivateFileLoadInProgress = true;
	}
	
	public static void endFileLoad()
	{
		g_bPrivateFileLoadInProgress = false;
	}
	
	public static boolean isFileLoading()
	{
		return g_bPrivateFileLoadInProgress;
	}
	
	// NET PACKET
	public static void beginNetPacketProcessing()
	{
		g_bNetPacketInProgress = true;
	}
	
	public static void endNetPacketProcessing()
	{
		g_bNetPacketInProgress = false;
	}

	public static boolean isNetPacketProcessing()
	{
		return g_bNetPacketInProgress;
	}
	

	// this is trus if we are processing an external network action
	private static boolean g_bNetPacketInProgress;
	   
	// this is true if we're loading a file from disk to the private layer
	private static boolean g_bPrivateFileLoadInProgress;
	   
	// this is true if we are in the process of receiving the
	// inital map data from the host after logging in. 
	private static boolean g_bInitalHostDumpInProgress;
}