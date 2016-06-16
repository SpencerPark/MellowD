package cas.cs4tb3.mellowd.intermediate;

import java.util.LinkedList;
import java.util.List;

public class Phrase implements Playable {
    private Sound lastAddedSound;
    protected final List<Playable> elements;

    public Phrase() {
        this.elements = new LinkedList<>();
    }

    public Phrase(Phrase toCopy) {
        this.elements = new LinkedList<>(toCopy.elements);
    }

    public void addElement(Playable playable) {
        if (playable instanceof Sound) {
            Sound next = (Sound) playable;
            if (this.lastAddedSound != null) {
                this.lastAddedSound.setNext(next);
            }
            this.lastAddedSound = next;
        }
        this.elements.add(playable);
    }

    @Override
    public void play(MIDIChannel channel) {
        elements.forEach(p -> p.play(channel));
    }
}
