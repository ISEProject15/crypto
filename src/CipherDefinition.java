public interface CipherDefinition {
    public Cipher create(CipherRequest request);
    public String identity();
}