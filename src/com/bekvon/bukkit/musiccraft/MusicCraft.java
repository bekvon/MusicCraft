/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.musiccraft;

import java.io.File;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.NoteBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Administrator
 */
public class MusicCraft extends JavaPlugin {

    private static MusicCraftBlockListener blistener;
    private static MusicCraftPlayerListener plistener;
    private static MMLManager songManager;
    public static Permission perms = null;

    public void onDisable() {
        Logger.getLogger("Minecraft").log(Level.INFO, "[MusicCraft] Disabling!");
        songManager.removeAllPerformers();
    }

    public void onEnable() {
        Logger.getLogger("Minecraft").log(Level.INFO, "[MusicCraft] Enabling! Version: " + this.getDescription().getVersion() + " by bekvon");
        this.getConfig();
        setupPermissions();
        if (songManager == null) {
            plistener = new MusicCraftPlayerListener(this);
            blistener = new MusicCraftBlockListener(this);
            getServer().getPluginManager().registerEvents(blistener, this);
            getServer().getPluginManager().registerEvents(plistener, this);
            songManager = new MMLManager(this.getConfig(), new File(this.getDataFolder(), "songs"));
        } else {
            songManager.readConfiguration(this.getConfig());
        }
    }

    public static MMLManager getManager()
    {
        return songManager;
    }

    public static Permission getAuthorityManager()
    {
        return perms;
    }

    public static boolean hasAuthority(Player player, String permission, boolean def)
    {
        if(player.hasPermission(permission))
            return true;
        if(perms == null)
            return def;
        else
            return perms.playerHas(player, permission);
        
        
    }

  

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if(sender instanceof Player)
        {
            Player player = (Player)sender;
            if(cmd.getName().equals("mml") && MusicCraft.hasAuthority(player, "musiccraft.use", true))
            {
                if(args.length == 0)
                    return false;
                if(args[0].equals("save"))
                {
                    if(args.length<3)
                        return false;
                    if(songManager.getFileManager().getSongExists(args[1]))
                    {
                        String composer = songManager.getFileManager().getSongComposer(args[1]);
                        if(composer!=null)
                        {
                            if(!composer.equals("") && !composer.equals(player.getName()))
                            {
                                player.sendMessage("§cThis song name exists and does not belong to you...");
                                return true;
                            }
                        }
                    }
                    if(!args[2].startsWith("[MML]"))
                        args[2] = "[MML]" + args[2];
                    songManager.getFileManager().saveSong(args[2], player.getName(), args[1]);
                    player.sendMessage("§eSong Saved...");
                    return true;
                }
                else if(args[0].equals("play"))
                {
                    if(args.length<2)
                        return false;
                    String readSong = songManager.getFileManager().getSongMML(args[1]);
                    Block nb = BlockHelper.findNearbyBlock(player.getWorld(), player.getLocation(), Material.NOTE_BLOCK.getId(), 1);
                    if(nb!=null)
                    {
                        if(readSong!=null)
                        {
                            NoteBlock noteBlock = (NoteBlock)nb.getState();
                            songManager.playMMLSong(readSong, noteBlock,player);
                        }
                        else
                        {
                            player.sendMessage("§cSong not found...");
                        }
                    }
                    else
                    {
                        player.sendMessage("§cNo nearby note block found...");
                    }
                    return true;
                }
                else if (args[0].equals("stop")) {
                    Block block = BlockHelper.findNearbyBlock(player.getWorld(), player.getLocation(), Material.NOTE_BLOCK.getId(), 1);
                    if (block != null) {
                        NoteBlock nb = (NoteBlock) block.getState();
                        if (nb != null) {
                            if (MusicCraft.getManager().isBlockPlaying(nb)) {
                                Player thisPlayer = MusicCraft.getManager().getPerformer((NoteBlock) nb).getOwner();
                                if (thisPlayer == null || player == thisPlayer) {
                                    MusicCraft.getManager().stopMMLSong(nb);
                                    player.sendMessage("§eStopped playing...");
                                }
                            } else {
                                player.sendMessage("§cNote block not playing...");
                            }
                        } else {
                            player.sendMessage("§cNote block not found...");
                        }
                    } else {
                        player.sendMessage("§cNote block not found...");
                    }
                }
                else if(args[0].equals("list"))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Song List:§e ");
                    ArrayList songList = songManager.getFileManager().getSongList();
                    if(args.length==2)
                    {
                        try
                        {
                            int page = Integer.parseInt(args[1]);
                            if(page < 1)
                            {
                                player.sendMessage("Invalid Page Number...");
                                return true;
                            }
                            int start = (page - 1) * 10;
                            int end = start + 10;
                            if(end >= songList.size())
                                end = songList.size() - 1;
                            sb.append("§a<Page ").append(page).append(" of ").append((int)Math.ceil(((float)songList.size()-1) / ((float)10))).append(">§e ");
                            for(int i = start; i < end; i++)
                            {
                                sb.append(songList.get(i)).append(" ");
                            }
                        }
                        catch (Exception ex)
                        {
                            player.sendMessage("Invalid page number...");
                            return true;
                        }
                    }
                    else
                    {
                        for(int i = 0; i < songList.size(); i ++)
                        {
                            sb.append(songList.get(i)).append(" ");
                        }
                    }
                    player.sendMessage(sb.toString());
                    return true;
                }
                else if(args[0].equals("info"))
                {
                    if(args.length<2)
                        return false;
                    String composer = songManager.getFileManager().getSongComposer(args[1]);
                    if(composer!=null && composer.equals(player.getName()))
                        player.sendMessage("§eScript: " + songManager.getFileManager().getSongMML(args[1]));
                    else
                        player.sendMessage("§eComposer: " + composer);
                    return true;
                }
                else if(args[0].equals("remove"))
                {
                    if(args.length<2)
                        return false;
                    if(songManager.getFileManager().getSongExists(args[1]))
                    {
                        if(songManager.getFileManager().getSongComposer(args[1]).equals(player.getName()) || (MusicCraft.hasAuthority(player, "musiccraft.songs.admin", false)))
                        {
                             songManager.getFileManager().removeSong(args[1]);
                             player.sendMessage("§eSong removed...");
                             return true;
                        }
                        else
                        {
                            player.sendMessage("§cYou do not have permission...");
                            return true;
                        }
                    }
                    else
                    {
                        player.sendMessage("§cError, song does not exist...");
                        return true;
                    }
                }
                else if(args[0].equals("import"))
                {
                    if(!MusicCraft.hasAuthority(player, "musiccraft.songs.admin", player.isOp()))
                    {
                        player.sendMessage("You don't have permission to parse midi songs.");
                        return true;
                    }
                    if(args.length<2)
                        return false;
                    File songFolder = songManager.getFileManager().getSongDir();
                    File midiFile = new File(songFolder, args[1] + ".mid");
                    if(args[1].equals("*"))
                    {
                        File[] list = songFolder.listFiles();
                        for(int i = 0; i < list.length; i ++)
                        {
                            if(list[i].isFile())
                            {
                                String name = list[i].getName();
                                if(name.endsWith(".mid"))
                                {
                                    name = name.substring(0,name.length()-4);
                                    args[1] = name;
                                    this.onCommand(sender, cmd, commandLabel, args);
                                }
                            }
                        }
                        return true;
                    }
                    if(midiFile.isFile())
                    {
                        Map<String, String> midiMML = MIDIConverter.getMMLFromMidi(midiFile, args[1]);
                        Iterator<Entry<String, String>> trackIt = midiMML.entrySet().iterator();
                        while(trackIt.hasNext())
                        {
                            Entry<String, String> next = trackIt.next();
                            if(songManager.getFileManager().getSongExists(next.getKey()))
                            {
                                songManager.getFileManager().removeSong(next.getKey());
                            }
                            songManager.getFileManager().saveSong(next.getValue(), player.getName(), next.getKey());
                            player.sendMessage("Saved: " + next.getKey() + ".mml");
                        }
                    } else {
                        player.sendMessage("MIDI file: "+args[1]+".mid not found.");
                    }
                    return true;
                }
                return false;
            }
            return true;
        }
        return super.onCommand(sender, cmd, commandLabel, args);
    }

    public void onLoad() {
        System.out.println("MusicCraft Loading...");
    }
    
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
}
