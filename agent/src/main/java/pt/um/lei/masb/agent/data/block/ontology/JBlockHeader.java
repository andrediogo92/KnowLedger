package pt.um.lei.masb.agent.data.block.ontology;

import jade.content.Concept;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Objects;


public final class JBlockHeader implements Concept {
    private String difficulty;
    private long blockheight;
    private String hash;
    private String merkleRoot;
    private String previousHash;
    private String timeStamp;
    private long nonce;

    public JBlockHeader(@NotEmpty String difficulty,
                        long blockheight,
                        @NotEmpty String hash,
                        @NotEmpty String merkleRoot,
                        @NotNull String previousHash,
                        @NotEmpty String timeStamp,
                        long nonce) {
        this.difficulty = difficulty;
        this.blockheight = blockheight;
        this.hash = hash;
        this.merkleRoot = merkleRoot;
        this.previousHash = previousHash;
        this.timeStamp = timeStamp;
        this.nonce = nonce;
    }

    public @NotEmpty String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(@NotEmpty String difficulty) {
        this.difficulty = difficulty;
    }

    public long getBlockheight() {
        return blockheight;
    }

    public void setBlockheight(long blockheight) {
        this.blockheight = blockheight;
    }

    public @NotEmpty String getHash() {
        return hash;
    }

    public void setHash(@NotEmpty String hash) {
        this.hash = hash;
    }

    public @NotEmpty String getMerkleRoot() {
        return merkleRoot;
    }

    public void setMerkleRoot(@NotEmpty String merkleRoot) {
        this.merkleRoot = merkleRoot;
    }

    public @NotEmpty String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(@NotEmpty String previousHash) {
        this.previousHash = previousHash;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JBlockHeader that = (JBlockHeader) o;
        return blockheight == that.blockheight &&
                timeStamp == that.timeStamp &&
                nonce == that.nonce &&
                Objects.equals(difficulty, that.difficulty) &&
                Objects.equals(hash, that.hash) &&
                Objects.equals(merkleRoot, that.merkleRoot) &&
                Objects.equals(previousHash, that.previousHash);
    }

    @Override
    public int hashCode() {

        return Objects.hash(difficulty, blockheight, hash, merkleRoot, previousHash, timeStamp, nonce);
    }

    @Override
    public @NotEmpty String toString() {
        return "JBlockHeader{" +
                "difficulty='" + difficulty + '\'' +
                ", blockheight=" + blockheight +
                ", hash='" + hash + '\'' +
                ", merkleRoot='" + merkleRoot + '\'' +
                ", previousHash='" + previousHash + '\'' +
                ", timeStamp=" + timeStamp +
                ", nonce=" + nonce +
                '}';
    }
}
