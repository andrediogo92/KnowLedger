package pt.um.lei.masb.agent.data.block.ontology;

import java.util.List;
import java.util.Objects;

public final class JMerkleTree {
    private List<String> hashes;
    private List<Integer> levelIndex;

    public JMerkleTree(List<String> hashes, List<Integer> levelIndex) {
        this.hashes = hashes;
        this.levelIndex = levelIndex;
    }

    public List<String> getHashes() {
        return hashes;
    }

    public void setHashes(List<String> hashes) {
        this.hashes = hashes;
    }

    public List<Integer> getLevelIndex() {
        return levelIndex;
    }

    public void setLevelIndex(List<Integer> levelIndex) {
        this.levelIndex = levelIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JMerkleTree that = (JMerkleTree) o;
        return Objects.equals(hashes, that.hashes) &&
                Objects.equals(levelIndex, that.levelIndex);
    }

    @Override
    public int hashCode() {

        return Objects.hash(hashes, levelIndex);
    }
}
