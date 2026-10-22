package com.example.warehouse_logistics.web.form;

import com.example.warehouse_logistics.dto.ReceiveLineRequest;
import com.example.warehouse_logistics.dto.ReceiveRequest;

import java.util.ArrayList;
import java.util.List;

public class ReceiveForm {

    private List<ReceiveLineForm> lines = new ArrayList<>();

    public ReceiveRequest toRequest() {
        List<ReceiveLineRequest> received = lines.stream()
                .filter(line -> line.getLineId() != null && line.getQuantity() != null && line.getQuantity() > 0)
                .map(line -> new ReceiveLineRequest(line.getLineId(), line.getQuantity()))
                .toList();
        return new ReceiveRequest(received);
    }

    public List<ReceiveLineForm> getLines() {
        return lines;
    }

    public void setLines(List<ReceiveLineForm> lines) {
        this.lines = lines;
    }
}
