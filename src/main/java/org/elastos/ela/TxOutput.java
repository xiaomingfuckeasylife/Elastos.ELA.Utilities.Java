package org.elastos.ela;

import org.elastos.ela.bitcoinj.Utils;

import javax.xml.bind.DatatypeConverter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by nan on 18/1/10.
 */
public class TxOutput {

    private static final byte DEFAULT_OUTPUT_TYPE = 0x00;
    private static final byte VOTE_OUTPUT_TYPE = 0x01;

    interface OutputPayload{
        void Serialize(DataOutputStream o) throws IOException;
        byte GetVersion();
    }

    class DefaultOutput implements OutputPayload{
        public void Serialize(DataOutputStream o) throws IOException{

        };
        public byte GetVersion(){
            return 0x00;
        };
    }

    class VoteOutput implements OutputPayload{
        private byte Version;
        private VoteContent[] Contents;
        public VoteOutput(VoteContent[] contents){
            this.Contents = contents;
        }
        public void Serialize(DataOutputStream o) throws IOException{
            o.writeByte(this.Version);
            Util.WriteVarUint(o,this.Contents.length);
            for (int i=0;i<this.Contents.length;i++){
                this.Contents[i].Serialize(o);
            }
        }
        public byte GetVersion(){
            return Version;
        }
    }
    class VoteContent{
        private byte VoteType;
        private byte[][] Candidates;
        public VoteContent(byte voteType,byte[][] Candidates){
            this.VoteType = voteType;
            this.Candidates = Candidates;
        }
        public byte getVoteType() {
            return VoteType;
        }

        public void setVoteType(byte voteType) {
            VoteType = voteType;
        }

        public byte[][] getCandidates() {
            return Candidates;
        }

        public void setCandidates(byte[][] candidates) {
            Candidates = candidates;
        }

        public void Serialize(DataOutputStream o) throws IOException{
            o.writeByte(this.VoteType);
            Util.WriteVarUint(o,this.Candidates.length);
            for(int i=0;i<this.Candidates.length;i++){
                Util.WriteVarBytes(o,this.Candidates[i]);
            }
        }
    }

    private byte[] AssetID= Common.ELA_ASSETID; //32 byte unit256
    private long Value; //Fixed64
    private long OutputLock = 0; //uint32
    private byte[] ProgramHash; //21byte unit168
    private String Address;
    private final String DESTROY_ADDRESS = "0000000000000000000000000000000000";
    private byte OutputType;
    private OutputPayload OutputPayload;
    @Override
    public String toString() {
        return "TxOutput{" +
                "AssetID=" + Arrays.toString(AssetID) +
                ", Value=" + Value +
                ", OutputLock=" + OutputLock +
                ", ProgramHash=" + Arrays.toString(ProgramHash) +
                ", Address='" + Address + '\'' +
                ", DESTROY_ADDRESS='" + DESTROY_ADDRESS + '\'' +
                '}';
    }

    /**
     *
     * @param address 地址
     * @param amount 金额
     */
    public TxOutput(String address,long amount){
        this.Address = address;
        this.Value = amount;
        if (address.equals(DESTROY_ADDRESS)){
            this.ProgramHash = new byte[21];
        }else {
            this.ProgramHash = Util.ToScriptHash(address);
        }
    }

    /**
     *
     * @param address 地址
     * @param amount 金额
     */
    public TxOutput(String address,long amount,String[] candidates,byte outputType){
        this.Address = address;
        this.Value = amount;
        if (address.equals(DESTROY_ADDRESS)){
            this.ProgramHash = new byte[21];
        }else {
            this.ProgramHash = Util.ToScriptHash(address);
        }
        this.OutputType = outputType;
        byte[][] cp = new byte[candidates.length][33];
        for(int i=0;i<candidates.length;i++){
            byte[] pub = DatatypeConverter.parseHexBinary(candidates[i]);
            cp[i] = pub;
        }
        VoteContent vc = new VoteContent((byte)0x00,cp);
        this.OutputPayload = new VoteOutput(new VoteContent[]{vc});
    }


    void Serialize(DataOutputStream o,byte txVersion) throws IOException {
        o.write(this.AssetID);
        o.writeLong(Long.reverseBytes(this.Value));
        o.writeInt(Integer.reverseBytes((int)this.OutputLock));
        o.write(this.ProgramHash);

        o.writeByte(this.OutputType);
        if(txVersion >= 0x09 && this.OutputPayload != null){
            this.OutputPayload.Serialize(o);
        }
    }

    public static Map DeSerialize(DataInputStream o) throws IOException {
        // AssetID
        byte[] buf = new byte[32];
        o.read(buf,0,32);
        DatatypeConverter.printHexBinary(Utils.reverseBytes(buf));

        // Value
        long value =  o.readLong();
        long v = Long.reverseBytes(value);

        // OutputLock
        long outputLock =  o.readInt();
        Long.reverseBytes(outputLock);

        // ProgramHash
        byte[] program = new byte[21];
        o.read(program,0,21);
        byte[] programHash = program;
        String address = Util.ToAddress(programHash);

        Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
        outputMap.put("Address:",address);
        outputMap.put("Value:",v);
        return outputMap;
    }

    public byte[] getAssetID() {
        return AssetID;
    }

    public long getValue() {
        return Value;
    }

    public long getOutputLock() {
        return OutputLock;
    }

    public byte[] getProgramHash() {
        return ProgramHash;
    }

    public String getAddress() {
        return Address;
    }

    public byte getVersion() throws Exception{
        if ( this.OutputType == (byte)0x00){
           return 0x00;
        }else if(this.OutputType == (byte)0x01){
          return 0x09;
        }
        throw new RuntimeException("Unrecognized output type");
    }
}
