/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.musiccraft;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.NoteBlock;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 *
 * @author Administrator
 */
public class MMLManager {
    private Map<Location,MMLPerformer> MMLPerformers;
    private Map<Location,Long> locationCooldown;
    private long locationCooldownTimer=1000;
    private int minTempo=100;
    private boolean allowRepeat=true;
    private int maxGlobal=30;
    private int maxPerPlayer=5;
    private int maxOwnerless=15;
    private boolean allowRedstone=true;
    private int maxOctet=2;
    private boolean ownerlessRepeat=true;
    private boolean removeOnLogout=true;
    private MMLFileManager fileManager;
    private MIDIConverter midiConverter;

    public MMLManager(Configuration config, File songSaveLocation)
    {
        MMLPerformers = Collections.synchronizedMap(new HashMap<Location,MMLPerformer>());
        locationCooldown = Collections.synchronizedMap(new HashMap<Location,Long>());
        readConfiguration(config);
        fileManager = new MMLFileManager(songSaveLocation);
        midiConverter = new MIDIConverter();
    }

    public synchronized void readConfiguration(Configuration config) {
        locationCooldownTimer = config.getInt("noteBlockCooldownTimer", 1000);
        minTempo = config.getInt("minTempo", 100);
        allowRepeat = config.getBoolean("allowRepeat", true);
        maxGlobal = config.getInt("globalConcurrentLimit", 30);
        maxPerPlayer = config.getInt("perPlayerConcurrentLimit", 5);
        allowRedstone = config.getBoolean("allowRedstone", true);
        maxOctet = config.getInt("maxOctet", 2);
        maxOwnerless = config.getInt("maxOwnerlessConcurrent", 15);
        ownerlessRepeat = config.getBoolean("allowOwnerlessRepeat", true);
        removeOnLogout = config.getBoolean("stopSongsOnLogout", true);
    }

    public synchronized void playMMLSong(String mmlSong, NoteBlock block)
    {
        playMMLSong(mmlSong,block,null);
    }

    public synchronized void playMMLSong(String mmlSong, NoteBlock block, Player owner)
    {
        Location blockLoc = block.getBlock().getLocation();
        if(!locationCooldownExpired(blockLoc))
        {
            if(owner!=null)
            {
                if(!MusicCraft.hasAuthority(owner, "musiccraft.ignore.cooldowntimer", false))
                {
                    owner.sendMessage("§cNoteBlock cooldown timer has not expired...");
                    return;
                }
            }
            else
                return;
        }
        setLocationCooldown(blockLoc);
        removeDeadPerformers();
        if(isBlockAssigned(block))
        {
            stopMMLSong(block);
        }
        if(MMLPerformers.size() >= maxGlobal)
        {
            if(owner!=null)
                owner.sendMessage("§cServer maximum concurrent song limit reached...");
            return;
        }
        if(owner!=null)
        {
            int count = getPlayerPerformersCount(owner);
            if(count>=maxPerPlayer)
            {
                if(!MusicCraft.hasAuthority(owner, "musiccraft.ignore.playerlimit", false))
                {
                    owner.sendMessage("§cYou reached your max concurrent playing songs...");
                    return;
                }
            }
        }
        else
        {
            int count = this.getOwnerlessPerformersCount();
            if(count>=maxOwnerless)
            {
                return;
            }
        }
        MMLSong song = MMLSong.parseMML(mmlSong);
        if(song.isRepeat())
        {
            if(owner == null)
            {
                if(!allowRepeat || !ownerlessRepeat)
                {
                    song.setRepeat(false);
                }
            }
            else
            {
                if(!allowRepeat && !MusicCraft.hasAuthority(owner, "musiccraft.ignore.repeat", allowRepeat))
                    song.setRepeat(false);
            }
        }
        if(owner == null)
        {
            song.setMinTempo(minTempo);
        }
        else
        {
            if(!MusicCraft.hasAuthority(owner, "musiccraft.ignore.mintempo",false))
            {
                song.setMinTempo(minTempo);
            }
        }
        MMLPerformer newPerformer = new MMLPerformer(song,block,owner,this);
        newPerformer.startPlaying();
        MMLPerformers.put(blockLoc, newPerformer);
        if(owner != null)
            owner.sendMessage("§aStarted playing, Notes: " + song.getNoteCount());
    }

    public synchronized void stopMMLSong(NoteBlock block)
    {
        stopMMLSong(block,null);
    }

    public synchronized void stopMMLSong(NoteBlock block, Player player)
    {
        Location blockLoc = block.getBlock().getLocation();
        if (MMLPerformers.containsKey(blockLoc)) {
            MMLPerformer thisPlayer = MMLPerformers.get(blockLoc);
            thisPlayer.stopPlaying();
            MMLPerformers.remove(blockLoc);
            if(player!=null)
                player.sendMessage("§cStopped playing...");
        }
    }

    public synchronized boolean isBlockAssigned(NoteBlock block)
    {
        if(block != null)
        {
            Block thisBlock = block.getBlock();
            if(thisBlock != null)
            {
                return MMLPerformers.containsKey(thisBlock.getLocation());
            }
            return false;
        }
        return false;
    }

    public synchronized boolean isBlockPlaying(NoteBlock block)
    {
        if(isBlockAssigned(block))
        {
            MMLPerformer thisPerformer = MMLPerformers.get(block.getBlock().getLocation());
            return thisPerformer.isPlaying();
        }
        return false;
    }

    public synchronized MMLPerformer getPerformer(NoteBlock block)
    {
        if(isBlockAssigned(block))
            return MMLPerformers.get(block.getBlock().getLocation());
        return null;
    }

    public synchronized boolean locationCooldownExpired(Location location)
    {
        if(locationCooldown.containsKey(location))
        {
            Long lastTime = locationCooldown.get(location);
            if(System.currentTimeMillis() >= lastTime)
                return true;
            return false;
        }
        return true;
    }

    public synchronized void removeLocationCooldown(Location location)
    {
        if(locationCooldown.containsKey(location))
        {
            locationCooldown.remove(location);
        }
    }

    public synchronized void setLocationCooldown(Location location)
    {
        if(location != null)
            locationCooldown.put(location, System.currentTimeMillis() + locationCooldownTimer);
    }

    public synchronized ArrayList<MMLPerformer> getPlayersPerformers(Player player)
    {
        ArrayList<MMLPerformer> list = new ArrayList<MMLPerformer>();
        Iterator<MMLPerformer> pIt = MMLPerformers.values().iterator();
        while (pIt.hasNext()) {
            MMLPerformer next = pIt.next();
            if (next.getOwner() == player) {
                list.add(next);
            }
        }
        return list;
    }

    public synchronized int getPlayerPerformersCount(Player player)
    {
        int count=0;
        Iterator<MMLPerformer> pIt = MMLPerformers.values().iterator();
        while(pIt.hasNext())
        {
            if(pIt.next().getOwner() == player)
            {
                count++;
            }
        }
        return count;
    }

    public synchronized void removePlayersPerformers(Player player)
    {
        ArrayList<MMLPerformer> playerPerformers = getPlayersPerformers(player);
        for(int i = 0; i < playerPerformers.size(); i++)
        {
            MMLPerformer performer = playerPerformers.get(i);
            performer.stopPlaying();
            MMLPerformers.remove(performer.getNoteBlock().getBlock().getLocation());
            locationCooldown.remove(performer.getNoteBlock().getBlock().getLocation());
        }
    }

    public synchronized int getOwnerlessPerformersCount()
    {
        return getPlayerPerformersCount(null);
    }

    public synchronized ArrayList<MMLPerformer> getOwnerlessPerformers()
    {
        return getPlayersPerformers(null);
    }

    public synchronized void removeOwnerlessPerformers()
    {
        removePlayersPerformers(null);
    }

    public synchronized void removeDeadPerformer(NoteBlock block)
    {
        if(isBlockAssigned(block))
        {
            if(!isBlockPlaying(block))
            {
                MMLPerformers.remove(block.getBlock().getLocation());
                locationCooldown.remove(block.getBlock().getLocation());
            }
        }
    }

    public synchronized void removeDeadPerformers()
    {
        Iterator<MMLPerformer> it = MMLPerformers.values().iterator();
        while(it.hasNext())
        {
            MMLPerformer next = it.next();
            if(!next.isPlaying())
            {
                it.remove();
                locationCooldown.remove(next.getNoteBlock().getBlock().getLocation());
            }
        }
    }

   public synchronized void removeAllPerformers()
    {
        Iterator<MMLPerformer> it = MMLPerformers.values().iterator();
        while(it.hasNext())
        {
            it.next().stopPlaying();
            it.remove();
        }
        MMLPerformers.clear();
        locationCooldown.clear();
    }

   public synchronized MMLFileManager getFileManager()
    {
       return fileManager;
   }

   public int getMaxOctet()
   {
       return maxOctet;
   }

   public boolean allowRedstone()
    {
       return allowRedstone;
   }

   public boolean stopSongsOnQuit()
    {
       return removeOnLogout;
   }

   public MIDIConverter getMIDIConverter()
   {
       return midiConverter;
   }
}
