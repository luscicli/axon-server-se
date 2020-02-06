/*
 * Copyright (c) 2017-2019 AxonIQ B.V. and/or licensed to AxonIQ B.V.
 * under one or more contributor license agreements.
 *
 *  Licensed under the AxonIQ Open Source License Agreement v1.0;
 *  you may not use this file except in compliance with the license.
 *
 */

package io.axoniq.cli;

import io.axoniq.cli.json.ContextNode;
import io.axoniq.cli.json.NodeAndRole;
import io.axoniq.cli.json.RestResponse;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.axoniq.cli.CommandOptions.*;

/**
 * @author Marc Gathier
 */
public class RegisterContext extends AxonIQCliCommand {
    public static void run(String[] args) throws IOException {
        // check args
        CommandLine commandLine = processCommandLine(args[0],
                                                     args,
                                                     CONTEXT,
                                                     NODES,
                                                     ACTIVE_BACKUP_NODES,
                                                     PASSIVE_BACKUP_NODES,
                                                     MESSAGING_ONLY_NODES,
                                                     CommandOptions.TOKEN);

        String url = createUrl(commandLine, "/v1/context");

        ContextNode contextNode = new ContextNode();
        contextNode.setContext(commandLine.getOptionValue(CONTEXT.getOpt()));
        List<NodeAndRole> nodeRolesMap = new ArrayList<>();
        Set<String> definedNodes = new HashSet<>();
        addNodes(commandLine, NODES, "PRIMARY", definedNodes, nodeRolesMap);
        addNodes(commandLine, ACTIVE_BACKUP_NODES, "ACTIVE_BACKUP", definedNodes, nodeRolesMap);
        addNodes(commandLine, PASSIVE_BACKUP_NODES, "PASSIVE_BACKUP", definedNodes, nodeRolesMap);
        addNodes(commandLine, MESSAGING_ONLY_NODES, "MESSAGING_ONLY", definedNodes, nodeRolesMap);

        contextNode.setRoles(nodeRolesMap);

        try (CloseableHttpClient httpclient = createClient(commandLine)) {
            postJSON(httpclient, url, contextNode, 200, getToken(commandLine),
                     RestResponse.class);
        }
    }

    private static void addNodes(CommandLine commandLine, Option nodes, String role, Set<String> definedNodes,
                                 List<NodeAndRole> nodeRolesMap) {
        if (commandLine.hasOption(nodes.getOpt())) {
            for (String primary : commandLine.getOptionValues(nodes.getOpt())) {
                if (definedNodes.contains(primary)) {
                    throw new RuntimeException("Node can only be provided once");
                }
                nodeRolesMap.add(new NodeAndRole(primary, role));
                definedNodes.add(primary);
            }
        }
    }
}
