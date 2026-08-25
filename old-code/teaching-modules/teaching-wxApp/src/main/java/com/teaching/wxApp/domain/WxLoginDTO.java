package com.teaching.wxApp.domain;

import com.teaching.common.core.web.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "微信用户登录请求的JSON数据")
public class WxLoginDTO extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(
            description = "临时登录凭证，用于小程序前端与微信服务器、开发者服务器之间的身份验证，是实现用户登录的核心参数。",
            requiredMode = Schema.RequiredMode.REQUIRED, // 必传
            example = "031a81000000000000012345"
    )
    private String code;

    @Schema(
            description = "邀请码,非必填",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED, // 非必传
            example = "13838838388"
    )
    private Long  invitationCode;

    @Schema(
            description = "邀请岗位ID",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED, //非必传
            example = "1"
    ) private Long   positionId;

    @Schema(
            description = "手机号加密数据",
            requiredMode = Schema.RequiredMode.REQUIRED, // 必传
            example = "1"
    )
    private String encryptedData;

    @Schema(
            description = "加密偏量",
            requiredMode = Schema.RequiredMode.REQUIRED, // 必传
            example = "1"
    )
    private String  iv;

    private String phone;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getInvitationCode() {
        return invitationCode;
    }

    public void setInvitationCode(Long invitationCode) {
        this.invitationCode = invitationCode;
    }

    public Long getPositionId() {
        return positionId;
    }

    public void setPositionId(Long positionId) {
        this.positionId = positionId;
    }

    public String getEncryptedData() {
        return encryptedData;
    }

    public void setEncryptedData(String encryptedData) {
        this.encryptedData = encryptedData;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }
}
