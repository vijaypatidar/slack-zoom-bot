#!/usr/bin/env node
import "source-map-support/register";
import * as cdk from "aws-cdk-lib";
import { StageType } from "../lib/constants/Stage";
import { DynamoDBStack } from "../lib/stacks/DynamoDBStack";
import { ToolsSlackEbBotStack } from "../lib/stacks/ToolsBotEbStack";

export interface ToolsSlackBotStackProps extends cdk.StackProps {
  stage: StageType;
  appName: string;
}

const props: ToolsSlackBotStackProps = {
  stage: (process.env.stage as StageType) || "dev",
  appName: "tools-bot",
};

const app = new cdk.App({});

const dynamoDBStack = new DynamoDBStack(app, "ToolsBotDatabaseStack", {
  ...props,
});

const toolsSlackBotEbStack = new ToolsSlackEbBotStack(
  app,
  "ToolsBotElasticbeanStack",
  {
    ...props,
    dynamoDBStack: dynamoDBStack,
  }
);
toolsSlackBotEbStack.addDependency(dynamoDBStack);
