/**
 * Copyright (c) 2017-2018 The Elastos Developers
 * <p>
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.elastos.ela;

import javax.xml.bind.DatatypeConverter;
import javax.xml.crypto.Data;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import static org.elastos.ela.Util.WriteVarBytes;
import static org.elastos.ela.Util.WriteVarUint;

/**
 * clark
 * <p>
 * 10/13/18
 */
public class PayloadRegisterIdentification {
    private String Id;
    private String Sign;
    private RegisterIdentificationContent[] Contents;
    private String IdPrivKey;
    private String programHash;

    public String getProgramHash() {
        return programHash;
    }

    public void setProgramHash(String programHash) {
        this.programHash = programHash;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getIdPrivKey() {
        return IdPrivKey;
    }

    public void setIdPrivKey(String idPrivKey) {
        IdPrivKey = idPrivKey;
    }

    public String getSign() {
        return Sign;
    }

    public void setSign(String sign) {
        Sign = sign;
    }

    public RegisterIdentificationContent[] getContents() {
        return Contents;
    }

    public void setContents(RegisterIdentificationContent[] contents) {
        Contents = contents;
    }

    public class RegisterIdentificationContent{
        private String PATH;

        private RegisterIdentificationValue[] Values;

        public String getPATH() {
            return PATH;
        }

        public void setPATH(String PATH) {
            this.PATH = PATH;
        }

        public RegisterIdentificationValue[] getValues() {
            return Values;
        }

        public void setValues(RegisterIdentificationValue[] values) {
            Values = values;
        }

        @Override
        public String toString() {
            return "RegisterIdentificationContent{" +
                    "PATH='" + PATH + '\'' +
                    ", Values=" + Arrays.toString(Values) +
                    '}';
        }

        public void Serialize(DataOutputStream o) throws IOException{
            WriteVarUint(o, this.PATH.length());
            o.write(this.PATH.getBytes());

            WriteVarUint(o,this.Values.length);
            for(int i=0;i<this.Values.length;i++){
                this.Values[i].Serialize(o);
            }
        }
    }

    public class RegisterIdentificationValue {
        private String DataHash;
        private String Proof;

        public String getDataHash() {
            return DataHash;
        }

        public void setDataHash(String dataHash) {
            DataHash = dataHash;
        }

        public String getProof() {
            return Proof;
        }

        public void setProof(String proof) {
            Proof = proof;
        }

        @Override
        public String toString() {
            return "RegisterIdentificationValue{" +
                    "DataHash='" + DataHash + '\'' +
                    ", Proof='" + Proof + '\'' +
                    '}';
        }

        public void Serialize(DataOutputStream o) throws IOException{
            byte[] DataHashByte = DatatypeConverter.parseHexBinary(DataHash);
            if (DataHashByte.length != 32) {
                throw new RuntimeException("DataHash must be 32 bytes");
            }
            Uint256 uint256 = new Uint256(DataHashByte);
            uint256.Serialize(o);

            WriteVarUint(o, this.Proof.length());
            o.write(this.Proof.getBytes());

        }
    }

    @Override
    public String toString() {
        return "PayloadRegisterIdentification{" +
                "Id='" + Id + '\'' +
                ", Sign='" + Sign + '\'' +
                ", Contents=" + Arrays.toString(Contents) +
                '}';
    }

    public void Serialize(DataOutputStream o) throws IOException {

        WriteVarUint(o, this.Id.length());
        o.write(this.Id.getBytes());

        byte[] signByte = this.Sign.getBytes();
        WriteVarBytes(o,signByte);

        WriteVarUint(o, this.Contents.length);
        for(int i=0;i<this.Contents.length;i++){
            this.Contents[i].Serialize(o);
        }
    }

}
