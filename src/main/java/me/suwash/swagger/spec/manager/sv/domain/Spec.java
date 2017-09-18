package me.suwash.swagger.spec.manager.sv.domain;

import javax.validation.constraints.NotNull;

import me.suwash.swagger.spec.manager.sv.da.GitRepoRepository;
import me.suwash.swagger.spec.manager.sv.da.SpecRepository;
import me.suwash.swagger.spec.manager.sv.domain.gen.SpecGen;

public class Spec extends SpecGen {

    @NotNull
    private final GitRepoRepository gitRepoRepository;
    @NotNull
    private final SpecRepository specRepository;

    public Spec(
        final GitRepoRepository gitRepository,
        final SpecRepository specRepository,
        final String id,
        final Object payload) {

        super(id, payload);
        this.gitRepoRepository = gitRepository;
        this.specRepository = specRepository;
    }

    public void add() {
        // Git作業ディレクトリが作成されていない場合、初期化
        if(!gitRepoRepository.isExist()) gitRepoRepository.init();
        // specを追加
        specRepository.add(this);
        // 追加を反映
        gitRepoRepository.push();
    }

    public void update(final Object payload) {
        // 事前に更新を取得
        gitRepoRepository.pull();
        // specを更新
        this.payload = payload;
        specRepository.update(this);
        // 更新を反映
        gitRepoRepository.push();
    }

    public void delete() {
        // 事前に更新を取得
        gitRepoRepository.pull();
        // specを削除
        specRepository.delete(this.id);
        // 削除を反映
        gitRepoRepository.push();
    }
}
