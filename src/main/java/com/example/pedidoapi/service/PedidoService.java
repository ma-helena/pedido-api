package com.example.pedidoapi.service;

import com.example.pedidoapi.dto.ItemDTO;
import com.example.pedidoapi.dto.PedidoRequest;
import com.example.pedidoapi.dto.PedidoResponse;
import com.example.pedidoapi.model.Pedido;
import com.example.pedidoapi.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PedidoService {
    private final PedidoRepository repository;

    public Mono<PedidoResponse> salvar(PedidoRequest pedidoDTO) {
        String uid = UUID.randomUUID().toString();
        var itensEntity = pedidoDTO.itens().stream()
                .map(dto -> new Pedido.Item(dto.idProduto(), dto.quantidade()))
                .toList();
        var pedidoEntity = new Pedido(uid,
                itensEntity,
                LocalDateTime.now(),
                Pedido.Status.REALIZADO, BigDecimal.ZERO
        );
        repository.salvar(pedidoEntity);
        return Mono.defer(() -> {
            return  Mono.just(new PedidoResponse(uid,pedidoDTO.itens(), Pedido.Status.REALIZADO));
        });

    }
    public Mono<PedidoResponse> buscarPorId(String id){
        return Mono.defer(()-> Mono.justOrEmpty(repository.buscarPorId(id)))
        .subscribeOn(Schedulers.boundedElastic())
                .map(entidade -> {
                        var itensDTO = entidade.itens().stream()
                                .map(it -> new ItemDTO(it.idProduto(), it.quantidade()))
                                .toList();

                        return new PedidoResponse(entidade.id(), itensDTO, entidade.status());
                });



}



}
