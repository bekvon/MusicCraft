/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.musiccraft;

import org.bukkit.Note;
import org.bukkit.block.NoteBlock;
import org.bukkit.entity.Player;

/**
 *
 * @author Administrator
 */
public class MMLPerformer {
    private MMLManager parent;
    private MMLSong playSong;
    private NoteBlock playBlock;
    private Thread playThread;
    private Player owner;
    private boolean run;

    public MMLPerformer(MMLSong song, NoteBlock block, Player player)
    {
        owner = player;
        playSong = song;
        playBlock = block;
    }

    public MMLPerformer(MMLSong song,NoteBlock block, Player player, MMLManager manager)
    {
        owner = player;
        playSong = song;
        parent = manager;
        playBlock = block;
    }

    public synchronized void startPlaying()
    {
        run = true;
        playThread = new Thread(new Runnable() {
            public void run() {
                if (!playSong.isRepeat()) {
                    playSong();
                } else {
                    while (run) {
                        playSong();
                    }
                }
               run = false;
               if(parent!=null)
                 parent.removeDeadPerformer(playBlock);
            }
        });
        playThread.start();
    }

    public synchronized void stopPlaying()
    {
        run = false;
        if(playThread != null)
            playThread.interrupt();
    }

    public boolean isPlaying()
    {
        return run;
    }

    private void playSong() {
        if(playBlock == null)
            return;
        int i = 0;
        byte[] notes = playSong.getNotes();
        int[] delays = playSong.getNoteTempos();
        if(notes.length == 0 || notes.length!=delays.length)
            return;
        while (i < notes.length) {
            byte thisnote = notes[i];
            int thisdelay = delays[i];
            try {
                if (thisnote < 25 && thisnote >= 0) {
                    Note note = new Note(thisnote);
                    playBlock.setNote(note);
                    playBlock.play();
                }
                i++;
                Thread.sleep(thisdelay);
            } catch (InterruptedException ex) {
                return;
            }
        }
    }

    public MMLSong getSong()
    {
        return playSong;
    }

    public synchronized Player getOwner()
    {
        return owner;
    }

    public synchronized void setOwner(Player player)
    {
        owner = player;
    }

    public NoteBlock getNoteBlock()
    {
        return playBlock;
    }
}
