package pt.um.li.mas.blockchain.data;

import java.util.Objects;

public final class MerkleNode {
  private MerkleNode left;
  private MerkleNode right;
  private MerkleNode parent;
  private MerkleNode sibling;
  private String hash;

  public MerkleNode(String hash) {
    this.parent = null;
    this.left = null;
    this.right = null;
    this.sibling = null;
    this.hash = hash;
  }

  public MerkleNode getLeft() {
    return left;
  }

  public void setLeft(MerkleNode left) {
    this.left = left;
  }

  public MerkleNode getRight() {
    return right;
  }

  public void setRight(MerkleNode right) {
    this.right = right;
  }

  public MerkleNode getParent() {
    return parent;
  }

  public void setParent(MerkleNode parent) {
    this.parent = parent;
  }

  public MerkleNode getSibling() {
    return sibling;
  }

  public void setSibling(MerkleNode sibling) {
    this.sibling = sibling;
  }

  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MerkleNode that = (MerkleNode) o;
    return Objects.equals(left, that.left) &&
           Objects.equals(right, that.right) &&
           Objects.equals(parent, that.parent) &&
           Objects.equals(sibling, that.sibling) &&
           Objects.equals(hash, that.hash);
  }

  @Override
  public int hashCode() {
    return Objects.hash(left, right, parent, sibling, hash);
  }

  @Override
  public String toString() {
    return "MerkleNode{" +
           "left=" + left +
           ", right=" + right +
           ", parent=" + parent +
           ", sibling=" + sibling +
           ", hash='" + hash + '\'' +
           '}';
  }
}
