import * as cdk from "aws-cdk-lib";
import { AttributeType, Table } from "aws-cdk-lib/aws-dynamodb";
import * as elasticbeanstalk from "aws-cdk-lib/aws-elasticbeanstalk";
import * as iam from "aws-cdk-lib/aws-iam";
import { Construct } from "constructs";
import { StageType } from "./Stage";
import s3assets = require("aws-cdk-lib/aws-s3-assets");

export interface ToolsSlackBotStackProps extends cdk.StackProps {
  stage: StageType;
}
export class ToolsSlackBotStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props: ToolsSlackBotStackProps) {
    super(scope, id, props);
    const { stage } = props;
    const appName = "tools-bot";
    const accountsTable = new Table(this, `slack-tools-bot-accounts-${stage}`, {
      partitionKey: {
        name: "accountId",
        type: AttributeType.STRING,
      },
      tableName: `slack-tools-bot-accounts-${stage}`,
    });

    const bookingsTable = new Table(this, `slack-tools-bot-bookings-${stage}`, {
      partitionKey: {
        name: "bookingId",
        type: AttributeType.STRING,
      },
      tableName: `slack-tools-bot-bookings-${stage}`,
    });

    const EbInstanceRole = new iam.Role(
      this,
      `${appName}-aws-elasticbeanstalk-ec2-role`,
      {
        assumedBy: new iam.ServicePrincipal("ec2.amazonaws.com"),
      }
    );
    bookingsTable.grantReadWriteData(EbInstanceRole);
    accountsTable.grantReadWriteData(EbInstanceRole);

    const managedPolicy = iam.ManagedPolicy.fromAwsManagedPolicyName(
      "AWSElasticBeanstalkWebTier"
    );
    EbInstanceRole.addManagedPolicy(managedPolicy);

    const profileName = `${appName}-InstanceProfile`;
    const instanceProfile = new iam.CfnInstanceProfile(this, profileName, {
      instanceProfileName: profileName,
      roles: [EbInstanceRole.roleName],
    });

    // EBS Application and Environment
    const app = new elasticbeanstalk.CfnApplication(this, "Application", {
      applicationName: `${appName}-${stage}`,
    });

    const webAppZipArchive = new s3assets.Asset(this, "ToolsBotWebZip", {
      path: `${__dirname}/../../build/libs/slack-zoom-0.0.1-SNAPSHOT.jar`,
    });

    const appVersionProps = new elasticbeanstalk.CfnApplicationVersion(
      this,
      "AppVersion",
      {
        applicationName: app.applicationName!!,
        sourceBundle: {
          s3Bucket: webAppZipArchive.s3BucketName,
          s3Key: webAppZipArchive.s3ObjectKey,
        },
      }
    );
    appVersionProps.addDependency(app);

    interface Env {
      key: string;
      value: string;
    }

    console.log(process.env);
    const environmentVaribles: Env[] = [
      {
        key: "DB_ACCOUNTS_TABLE_NAME",
        value: accountsTable.tableName,
      },
      {
        key: "DB_BOOKINGS_TABLE_NAME",
        value: bookingsTable.tableName,
      },
      {
        key: "SLACK_BOT_TOKEN",
        value: "DUMMY_VALUE",
      },
      {
        key: "SLACK_SIGNING_SECRET",
        value: "DUMMY_VALUE",
      },
      {
        key: "BOT_UPDATE_CHANNEL_ID",
        value: "DUMMY_VALUE",
      },
    ];

    const buidOptionsFromEnv =
      (): elasticbeanstalk.CfnEnvironment.OptionSettingProperty[] => {
        return Object.values(environmentVaribles).map((env) => {
          return {
            namespace: "aws:elasticbeanstalk:application:environment",
            optionName: env.key,
            value: env.value,
          };
        });
      };

    const optionSettingProperties: elasticbeanstalk.CfnEnvironment.OptionSettingProperty[] =
      [
        {
          namespace: "aws:autoscaling:launchconfiguration",
          optionName: "IamInstanceProfile",
          value: instanceProfile.instanceProfileName,
        },
        {
          namespace: "aws:autoscaling:asg",
          optionName: "MinSize",
          value: "1",
        },
        {
          namespace: "aws:autoscaling:asg",
          optionName: "MaxSize",
          value: "1",
        },
        {
          namespace: "aws:ec2:instances",
          optionName: "InstanceTypes",
          value: "t2.micro",
        },
        ...buidOptionsFromEnv(),
        // {
        //   namespace: "elasticbeanstalk:application",
        //   optionName: "Application Healthcheck URL",
        //   value: "/health",
        // },
      ];

    const elbEnv = new elasticbeanstalk.CfnEnvironment(this, "Environment", {
      environmentName: `${appName}-${stage}-env`,
      applicationName: app.applicationName || appName,
      solutionStackName: "64bit Amazon Linux 2 v3.4.3 running Corretto 11",
      optionSettings: optionSettingProperties,
      versionLabel: appVersionProps.ref,
    });
    elbEnv.addDependency(app);
  }
}
