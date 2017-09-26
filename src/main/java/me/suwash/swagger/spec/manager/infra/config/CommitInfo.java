package me.suwash.swagger.spec.manager.infra.config;

import static me.suwash.swagger.spec.manager.infra.error.SpecMgrException.array;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.suwash.swagger.spec.manager.infra.constant.MessageConst;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

@AllArgsConstructor()
@Getter
public class CommitInfo {

    public CommitInfo(final String user, final String email) {
        this.user = user;
        this.email = email;
    }

    @JsonProperty("user")
    @NotEmpty
    protected String user = null;

    @JsonProperty("email")
    @NotEmpty
    protected String email = null;

    @JsonProperty("message")
    protected String message = null;

    public void canInit() {
        if (StringUtils.isEmpty(user)) {
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, array("user"));
        }
        if(StringUtils.isEmpty(email)) {
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, array("email"));
        }
    }

}
